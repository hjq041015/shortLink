package com.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.admin.dao.entity.UserDO;
import com.shortLink.admin.dto.resp.UserRespDTO;

/**
     * 用户信息接口类
     */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户返回信息实体
     */
     UserRespDTO getUserByUsername(String username);
}