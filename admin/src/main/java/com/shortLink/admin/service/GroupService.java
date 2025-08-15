package com.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.admin.dao.entity.GroupDO;
import com.shortLink.admin.dto.req.GroupSortReqDTO;
import com.shortLink.admin.dto.req.GroupUpdateReqDTO;
import com.shortLink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
 * 分组服务接口，定义分组相关的业务方法
 */
public interface GroupService extends IService<GroupDO> {
    /**
     * 增加分组
     */
    void addGroup(String name);

     /**
     * 新增短链接分组
     *
     * @param username  用户名
     * @param groupName 短链接分组名
     */
    void addGroup(String username, String groupName);

    /**
     * 查询分组
     */
    List<GroupRespDTO> queryGroup();

    /**
     *  修改分组
     */
    void updateGroup(GroupUpdateReqDTO requestParm);

    /**
     *  删除分组
     */
    void deleteGroup(String gid);

    /**
     *  排序分组
     */
    void sortGroup(List<GroupSortReqDTO> requestParm);
}

