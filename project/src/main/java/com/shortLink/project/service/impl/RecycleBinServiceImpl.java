package com.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dao.mapper.ShortLinkMapper;
import com.shortLink.project.dto.req.RecycleBinPageReqDTO;
import com.shortLink.project.dto.req.RecycleBinRecoverReqDTO;
import com.shortLink.project.dto.req.RecycleBinRemoveReqDTO;
import com.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.RecycleBinService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.shortLink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;
import static com.shortLink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;

@Service
@AllArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {
    private final StringRedisTemplate stringRedisTemplate;
    
    /**
     * 保存回收站信息
     * 
     * @param requestParm 回收站保存请求参数，包含分组ID和完整短链接
     */
    @Override
    public void save(RecycleBinSaveReqDTO requestParm) {
        // 构造更新条件，将指定短链接标记为回收站状态
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParm.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParm.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .set(ShortLinkDO::getEnableStatus, 1);
        this.baseMapper.update(null,updateWrapper);
        // 从Redis中删除对应的短链接缓存
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParm.getFullShortUrl()));
    }

    /**
     * 分页查询回收站短链接
     *
     * @param requestParm 回收站分页查询请求参数，包含分组ID列表等分页信息
     * @return 回收站短链接分页查询结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(RecycleBinPageReqDTO requestParm) {
         // 构造查询条件：根据分组ID列表查询已删除且在回收站中的短链接，并按更新时间倒序排列
         LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                 .in(ShortLinkDO::getGid,requestParm.getGidList())
                 .eq(ShortLinkDO::getDelFlag,0)
                 .eq(ShortLinkDO::getEnableStatus,1)
                 .orderByDesc(ShortLinkDO::getUpdateTime);
         IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParm, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 恢复短链接
     *
     * @param requestParm 回收站恢复请求参数，包含分组ID和完整短链接
     */
    @Override
    public void recover(RecycleBinRecoverReqDTO requestParm) {
        // 构造更新条件，将指定短链接从回收站状态恢复为正常状态
       LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParm.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParm.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .set(ShortLinkDO::getEnableStatus, 0);
        this.baseMapper.update(null,updateWrapper);
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParm.getFullShortUrl()));
    }

    /**
     * 删除短链接
     *
     * @param requestParm 回收站删除请求参数，包含分组ID和完整短链接
     */
    @Override
    public void remove(RecycleBinRemoveReqDTO requestParm) {
        // 构造删除条件：删除指定分组中处于回收站状态的短链接
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParm.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParm.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        this.baseMapper.delete(updateWrapper);
    }
}
