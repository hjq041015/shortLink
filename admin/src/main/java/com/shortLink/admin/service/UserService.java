package com.shortLink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.admin.dao.entity.UserDO;
import com.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.shortLink.admin.dto.resp.UserRespDTO;

    /**
     * 用户信息接口类
     */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名获取用户信息
     */
     UserRespDTO getUserByUsername(String username);


    /**
     * 检查用户名是否可用
     */
     Boolean availableUsername(String username);

     /**
      * 注册用户
      */
     void register(UserRegisterReqDTO requestParm);
}