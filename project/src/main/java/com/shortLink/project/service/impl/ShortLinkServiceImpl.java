package com.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.common.convention.exception.ServiceException;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dao.mapper.ShortLinkMapper;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
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
       return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));

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
