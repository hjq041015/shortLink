package com.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.admin.common.convention.exception.ClientException;
import com.shortLink.admin.dao.entity.UserDO;
import com.shortLink.admin.dao.mapper.UserMapper;
import com.shortLink.admin.dto.req.UserLoginReqDTO;
import com.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.shortLink.admin.dto.req.UserUpdateReqDTO;
import com.shortLink.admin.dto.resp.UserLoginRespDTO;
import com.shortLink.admin.dto.resp.UserRespDTO;
import com.shortLink.admin.service.UserService;
import lombok.AllArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shortLink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.shortLink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * 用户信息服务实现类
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> bloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名查询用户信息
     * 
     * @param username 用户名
     * @return 用户返回信息实体
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO,result);
        return result;
    }

    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @return 用户名可用返回true，否则返回false
     */
    @Override
    public Boolean availableUsername(String username) {
        return !bloomFilter.contains(username);
    }

    /**
     * 用户注册功能
     * 
     * @param requestParm 用户注册请求参数对象，包含用户名、密码等信息
     * @throws ClientException 当用户已存在或保存失败时抛出异常
     */
    @Override
    public void register(UserRegisterReqDTO requestParm) {
        // 检查用户名是否可用
        if(!availableUsername(requestParm.getUsername())) {
            throw new ClientException(USER_EXIST);
        }
        // 使用Redis分布式锁防止并发注册
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParm.getUsername());
        try{
            if (lock.tryLock()) {
                // 加锁后再次查库，确保用户名唯一
                Long count = this.lambdaQuery().eq(UserDO::getUsername, requestParm.getUsername()).count();
                if (count != null && count > 0) {
                    throw new ClientException(USER_EXIST);
                }
                int insert = baseMapper.insert(BeanUtil.toBean(requestParm, UserDO.class));
                if (insert < 1) {
                    throw new ClientException(USER_SAVE_ERROR);
                }
                bloomFilter.add(requestParm.getUsername());
                return; // 注册成功直接返回
            } else {
                // 获取锁失败，抛出用户已存在异常
                throw new ClientException(USER_EXIST);
            }
        }finally {
            // 释放锁
            lock.unlock();
        }
    }

    /**
     * 更新用户信息
     *
     * @param requestParm 用户信息更新请求参数
     */
    @Override
    public void update(UserUpdateReqDTO requestParm) {
        // TODO 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParm.getUsername());
        this.baseMapper.update(BeanUtil.toBean(requestParm,UserDO.class),updateWrapper);
    }

    /**
     * 用户登录功能
     *
     * @param requestParm 用户登录请求参数，包含用户名和密码
     * @return 用户登录响应参数，包含登录token
     */
    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParm) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,requestParm.getUsername())
                .eq(UserDO::getPassword,requestParm.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = this.baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(USER_NULL);
        }
        Boolean hasLogin = stringRedisTemplate.hasKey("login_" + requestParm.getUsername());
        if (hasLogin != null && hasLogin) {
            throw new ClientException("用户已登录");
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login_" + requestParm.getUsername(),uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire("login_" + requestParm.getUsername(),14, TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    /**
     * 检查用户是否登录
     *
     * @param username 用户名
     * @param token 登录凭证
     * @return 用户登录状态，已登录返回true，未登录返回false
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get("login_" + username,token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username,token)) {
            stringRedisTemplate.delete("login_" + username);
            return;
        }
       throw new ClientException("用户不存在或已退出登录");
    }
}

