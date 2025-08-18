package com.shortLink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shortLink.admin.common.convention.exception.ServiceException;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.dao.entity.GroupDO;
import com.shortLink.admin.dao.mapper.GroupMapper;
import com.shortLink.admin.remote.ShortLinkRemoteService;
import com.shortLink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.admin.service.RecycleBinService;
import com.shortLink.admin.toolkit.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {
    private final GroupMapper groupMapper;
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 回收站分页查询请求参数
     * @return 回收站短链接分页查询结果
     */
    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(RecycleBinPageReqDTO requestParam) {
        // 查询当前用户的所有分组信息
        LambdaQueryWrapper<GroupDO> queryWrappers = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername,UserContextHolder.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrappers);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        // 设置查询参数中的分组ID列表
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBinShortLink(requestParam);
    }
}
