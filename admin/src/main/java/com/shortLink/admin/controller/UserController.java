package com.shortLink.admin.controller;

import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.dto.resp.UserRespDTO;
import com.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 用户信息响应实体
     */
    @GetMapping("/api/shortLink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username")  String username) {
        return Results.success(userService.getUserByUsername(username));
    }

}