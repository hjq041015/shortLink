package com.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.common.convention.exception.ServiceException;
import com.shortLink.project.common.enums.VailDateTypeEnum;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dao.entity.ShortLinkGotoDO;
import com.shortLink.project.dao.mapper.ShortLinkGotoMapper;
import com.shortLink.project.dao.mapper.ShortLinkMapper;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.ShortLinkService;
import com.shortLink.project.toolkit.HashUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.shortLink.project.common.constant.RedisKeyConstant.*;

/**
 * 短链接服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    /**
     * 创建短链接
     *
     * @param requestParm 创建短链接的请求参数
     * @return 短链接创建响应结果
     */
    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParm) {
        String shortLinkSuffix = generateSuffix(requestParm);
        String fullShortUrl = StrBuilder.create(requestParm.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParm.getDomain())
                .originUrl(requestParm.getOriginUrl())
                .gid(requestParm.getGid())
                .createdType(requestParm.getCreatedType())
                .validDateType(requestParm.getValidDateType())
                .validDate(requestParm.getValidDate())
                .describe(requestParm.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .build();
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParm.getGid())
                .build();
        try{
            this.baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex) {
                throw new ServiceException(String.format("短链接: %s 生成重复",fullShortUrl));
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParm.getGid())
                .fullShortUrl("http://" +shortLinkDO.getFullShortUrl())
                .originUrl(requestParm.getOriginUrl())
                .build();
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询参数，包含分页信息和gid
     * @return 短链接分页查询结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> page = new Page<>(requestParam.getCurrent(),requestParam.getSize());
        // 构造查询条件：根据gid查询未删除且启用的短链接，并按创建时间倒序排列
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = new LambdaQueryWrapper<>(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        // 执行分页查询，将查询结果填充到 page 对象中
        IPage<ShortLinkDO> resultPage = this.baseMapper.selectPage(page, queryWrapper);
        // 将查询结果从 ShortLinkDO 转换为 ShortLinkPageRespDTO
       return resultPage.convert(each -> {
           ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
           result.setDomain("http://" + result.getDomain());
           return result;
       });

    }

    /**
     * 查询短链接组中短链接的数量统计
     *
     * @param requestParm 包含需要统计的短链接组ID列表
     * @return List<ShortLinkGroupCountRespDTO> 短链接组及其对应的短链接数量列表
     */
    @Override
    public List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParm) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid",requestParm)
                .eq("enable_status",0)
                .eq("del_flag",0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkCounts = shortLinkMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkCounts,ShortLinkGroupCountRespDTO.class);
    }

    /**
     * 更新短链接信息
     *
     * @param requestParm 短链接更新请求参数，包含完整短链接、分组ID、原始链接、有效期等信息
     */
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParm) {
        // 构造更新条件：完整短链接、分组ID、未删除、启用状态
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParm.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParm.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParm.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .originUrl(requestParm.getOriginUrl())
                .describe(requestParm.getDescribe())
                .validDate(requestParm.getValidDate())
                .validDateType(requestParm.getValidDateType())
                .build();
            baseMapper.update(shortLinkDO, updateWrapper);
    }

    /**
     * 短链接跳转到原始链接
     *
     * @param shortUrl 短链接
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUrl, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUrl;
        // 从Redis中获取原始链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY + fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            response.sendRedirect(originalLink);
            return;
        }
        // 通过布隆过滤器判断短链接是否存在，防止缓存穿透
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            return;
        }

        // 检查是否是已知的无效短链接
        String nullShortLink  = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY + fullShortUrl));
        if (StrUtil.isNotBlank(nullShortLink)) {
            return;
        }
        // 获取分布式锁，防止缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY + fullShortUrl));
        lock.lock();
        try {
            // 双重检查，再次从Redis中获取原始链接
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY + fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            response.sendRedirect(originalLink);
            return;
        }
        // 查询短链接跳转信息
        LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(gotoQueryWrapper);
        if (shortLinkGotoDO == null) {
            // 将无效短链接标识存入Redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
            return;
        }
        // 查询短链接详细信息
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl,fullShortUrl)
                .eq(ShortLinkDO::getGid,shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        if (shortLinkDO != null) {
            // 根据短链接找到原始链接进行跳转
            stringRedisTemplate.opsForValue().set(GOTO_SHORT_LINK_KEY + fullShortUrl,shortLinkDO.getOriginUrl());
            response.sendRedirect(shortLinkDO.getOriginUrl());
        }
        }finally {
            // 释放分布式锁
            lock.unlock();
        }

    }


    /**
     * 生成短链接后缀
     * <p>
     * 通过哈希算法生成短链接后缀，并通过布隆过滤器避免生成重复的短链接。
     * 如果生成的短链接已存在，则会重新生成，最多尝试10次。
     *
     * @param requestParam 短链接创建请求参数
     * @return 生成的短链接后缀
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            // 限制生成次数，避免无限循环
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成次数过多,请稍后再试");
            }
            String originalUrl = requestParam.getOriginUrl();
            originalUrl += UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originalUrl);
            // 检查生成的短链接是否已存在，如果不存在则跳出循环
            if(!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }
}
