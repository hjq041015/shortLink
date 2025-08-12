package com.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.admin.dao.entity.GroupDO;

/**
 * 分组服务接口，定义分组相关的业务方法
 */
public interface GroupService extends IService<GroupDO> {
    void addGroup(String name);
}

