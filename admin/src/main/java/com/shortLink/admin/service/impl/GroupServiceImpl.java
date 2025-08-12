package com.shortLink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.admin.common.convention.exception.ClientException;
import com.shortLink.admin.dao.entity.GroupDO;
import com.shortLink.admin.dao.mapper.GroupMapper;
import com.shortLink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.shortLink.admin.service.GroupService;
import com.shortLink.admin.toolkit.RandomGenerator;
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
    public List<ShortLinkGroupSaveReqDTO> queryGroup() {
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class)
                // TODO 从上下文获取用户名
                .eq(GroupDO::getDelFlag, 0)
                .isNull(GroupDO::getUsername)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        List<GroupDO> groupList = this.baseMapper.selectList(query);
        return BeanUtil.copyToList(groupList,ShortLinkGroupSaveReqDTO.class);
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
