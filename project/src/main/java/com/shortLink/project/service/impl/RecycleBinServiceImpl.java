package com.shortLink.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dao.mapper.ShortLinkMapper;
import com.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.shortLink.project.service.RecycleBinService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.shortLink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

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
}
