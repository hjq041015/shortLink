package com.shortLink.admin.dto.req;

import lombok.Data;

@Data
public class UserLoginReqDTO {

     /**
     * 用户名
     */
    String username;

    /**
     * 密码
     */
    String password;
}
