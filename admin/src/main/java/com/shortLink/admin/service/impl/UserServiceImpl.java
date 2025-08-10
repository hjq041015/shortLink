package com.shortLink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.admin.common.convention.exception.ClientException;
import com.shortLink.admin.common.enums.UserErrorCodeEnum;
import com.shortLink.admin.dao.entity.UserDO;
import com.shortLink.admin.dao.mapper.UserMapper;
import com.shortLink.admin.dto.resp.UserRespDTO;
import com.shortLink.admin.service.UserService;
import lombok.AllArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户信息服务实现类
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> bloomFilter;

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
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO,result);
        return result;


    }

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 用户名存在返回true，否则返回false
     */
    @Override
    public Boolean hasUsername(String username) {
        return bloomFilter.contains(username);
    }
}