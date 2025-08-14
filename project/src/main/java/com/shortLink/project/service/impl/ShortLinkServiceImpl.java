package com.shortLink.project.service.impl;

import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.common.convention.exception.ServiceException;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dao.mapper.ShortLinkMapper;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.service.ShortLinkService;
import com.shortLink.project.toolkit.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 短链接服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    /**
     * 创建短链接
     *
     * @param requestParm 创建短链接的请求参数
     * @return 短链接创建响应结果
     */
    @Override
    public ShortLinkCreateRespDTO crateShortLink(ShortLinkCreateReqDTO requestParm) {
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
        try{
            this.baseMapper.insert(shortLinkDO);
        }catch (DuplicateKeyException ex) {
            // 处理唯一索引冲突异常，检查是否确实重复
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = new LambdaQueryWrapper<>(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl,fullShortUrl);
            ShortLinkDO shortUri = this.baseMapper.selectOne(queryWrapper);
            if (shortUri != null) {
                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParm.getGid())
                .fullShortUrl(shortLinkDO.getFullShortUrl())
                .originUrl(requestParm.getOriginUrl())
                .build();
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
            originalUrl += System.currentTimeMillis();
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
