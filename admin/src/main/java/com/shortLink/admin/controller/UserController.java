package com.shortLink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.dto.resp.UserActualRespDTO;
import com.shortLink.admin.dto.resp.UserRespDTO;
import com.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/api/shortLink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username")  String username) {
        return Results.success(userService.getUserByUsername(username));
    }

    /**
     * 根据用户名获取用户未脱敏的信息
     */
    @GetMapping("/api/shortLink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getUserActualByUsername(@PathVariable("username")  String username) {
        return Results.success(BeanUtil.toBean(userService.getUserByUsername(username),UserActualRespDTO.class));
    }

    /**
     * 检查用户名是否存在
     */
    @GetMapping("/api/shortLinke/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userService.hasUsername(username));
    }

}