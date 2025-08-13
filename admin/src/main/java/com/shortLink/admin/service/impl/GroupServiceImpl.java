package com.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.admin.common.convention.exception.ClientException;
import com.shortLink.admin.dao.entity.GroupDO;
import com.shortLink.admin.dao.mapper.GroupMapper;
import com.shortLink.admin.dto.req.GroupSortReqDTO;
import com.shortLink.admin.dto.req.GroupUpdateReqDTO;
import com.shortLink.admin.dto.req.ShortLinkGroupAddReqDTO;
import com.shortLink.admin.service.GroupService;
import com.shortLink.admin.toolkit.RandomGenerator;
import com.shortLink.admin.toolkit.UserContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分组服务实现类，实现分组相关的业务逻辑
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    /**
     * 添加分组
     *
     * @param name 分组名称
     */
    @Override
    public void addGroup(String name) {
        // 先判断分组名称是否已存在，存在则抛出异常
        if (isGroupNameExists(name)) {
            throw new ClientException("分组名称已存在");
        }
        // 生成唯一gid
        String gid;
        do {
            gid = RandomGenerator.generateRandom();
        } while (isGidExists(gid));
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .username(UserContextHolder.getUsername())
                .name(name)
                .sortOrder(0)
                .build();
        this.baseMapper.insert(groupDO);
    }

    /**
     *
     * 查询分组列表
     * @return 分组列表
     */
    @Override
    public List<ShortLinkGroupAddReqDTO> queryGroup() {
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername,UserContextHolder.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupList = this.baseMapper.selectList(query);
        return BeanUtil.copyToList(groupList, ShortLinkGroupAddReqDTO.class);
    }

    /**
     * 修改分组名称
     *
     * @param requestParm 分组修改请求参数
     */
    @Override
    public void updateGroup(GroupUpdateReqDTO requestParm) {
       LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
        .eq(GroupDO::getUsername, UserContextHolder.getUsername())
        .eq(GroupDO::getGid, requestParm.getGid())
        .eq(GroupDO::getDelFlag, 0)
        .set(GroupDO::getName,requestParm.getName());
       this.baseMapper.update(null,updateWrapper);
    }

    /**
     * 删除分组
     *
     * @param gid 分组ID
     */
    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
        .eq(GroupDO::getUsername, UserContextHolder.getUsername())
        .eq(GroupDO::getGid,gid )
        .eq(GroupDO::getDelFlag, 0)
        .set(GroupDO::getDelFlag,1);
        this.baseMapper.update(null,updateWrapper);
    }

    /**
     * 分组排序
     *
     * @param requestParm 分组排序请求参数列表
     */
    @Override
    public void sortGroup(List<GroupSortReqDTO> requestParm) {
        requestParm.forEach(request-> {
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername,UserContextHolder.getUsername())
                    .eq(GroupDO::getGid,request.getGid())
                    .eq(GroupDO::getDelFlag,0)
                    .set(GroupDO::getSortOrder,request.getSortOrder());
            this.baseMapper.update(null,updateWrapper);
        });
    }

    /**
     * 判断分组名称是否已存在
     *
     * @param name 分组名称
     * @return true-存在，false-不存在
     */
    private boolean isGroupNameExists(String name) {
        LambdaQueryWrapper<GroupDO> queryWrappers = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getName, name);
        GroupDO group = this.baseMapper.selectOne(queryWrappers);
        return group != null;
    }

    /**
     * 判断gid是否已存在
     *
     * @param gid 分组标识
     * @return true-存在，false-不存在
     */
    private boolean isGidExists(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrappers = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid);
        GroupDO group = this.baseMapper.selectOne(queryWrappers);
        return group != null;
    }

}
