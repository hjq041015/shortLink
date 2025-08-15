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
import com.shortLink.admin.dto.resp.GroupRespDTO;
import com.shortLink.admin.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.admin.remote.ShortLinkRemoteService;
import com.shortLink.admin.service.GroupService;
import com.shortLink.admin.toolkit.RandomGenerator;
import com.shortLink.admin.toolkit.UserContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分组服务实现类，实现分组相关的业务逻辑
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

     ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

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
    public List<GroupRespDTO> queryGroup() {
        // 1) 构造查询条件：只查未删除(del_flag=0)、当前登录用户(username)的分组；
        //    并按 sort_order、update_time 倒序排
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getDelFlag, 0)
                .eq(GroupDO::getUsername,UserContextHolder.getUsername())
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);
        // 2) 执行查询，得到分组实体列表
        List<GroupDO> groupList = this.baseMapper.selectList(query);
        if (groupList == null || groupList.isEmpty()) {
            return Collections.emptyList();
        }
        // 3) 取出所有分组的 gid，远程调用短链接服务统计每个 gid 的短链数量
        //    注意：这里 shortLinkRemoteService 的返回是 Result<...>，所以取 .getData()
        List<ShortLinkGroupCountRespDTO> gids = shortLinkRemoteService.listGroupShortLinkCount(groupList.stream().map(GroupDO::getGid).toList()).getData();
        // 4) 把分组实体复制成对外返回的 DTO（属性名要能对上）
        List<GroupRespDTO> results = BeanUtil.copyToList(groupList, GroupRespDTO.class);
        // 5) 把 “gid -> 数量” 做成 Map，方便后面回填
        Map<String, Integer> counts = gids.stream().filter(Objects::nonNull).collect(Collectors.toMap(ShortLinkGroupCountRespDTO::getGid,
                dto-> dto.getShortLinkCount() == null ? 0: dto.getShortLinkCount()));
        // 6) 把每个分组对应的数量回填到返回 DTO 里（通过 gid 关联）
        return results.stream().peek(result-> result.setShortLinkCount(counts.get(result.getGid()))).toList();
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
