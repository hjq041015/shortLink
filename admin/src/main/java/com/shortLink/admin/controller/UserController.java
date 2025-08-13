package com.shortLink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.dto.req.UserLoginReqDTO;
import com.shortLink.admin.dto.req.UserRegisterReqDTO;
import com.shortLink.admin.dto.req.UserUpdateReqDTO;
import com.shortLink.admin.dto.resp.UserActualRespDTO;
import com.shortLink.admin.dto.resp.UserLoginRespDTO;
import com.shortLink.admin.dto.resp.UserRespDTO;
import com.shortLink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


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
     * 检查用户名是否可用
     */
    @GetMapping("/api/shortLink/v1/user/has-username")
    public Result<Boolean> availableUsername(@RequestParam("username") String username) {
        return Results.success(userService.availableUsername(username));
    }

    /**
     * 注册用户
     */
    @PostMapping("/api/shortLink/v1/user/register")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParm) {
        userService.register(requestParm);
        return Results.success();
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/api/shortLink/v1/user/update")
    public Result<Void> update(@RequestBody UserUpdateReqDTO requestParm) {
        userService.update(requestParm);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/api/shortLink/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParm) {
        return Results.success(userService.login(requestParm));
    }

    /**
     * 检查用户是否已经登录
     */
    @GetMapping("/api/shortLink/v1/user/check-login")
    public Result<Boolean> checkLogin() {
        return Results.success(userService.checkLogin());
    }

    /**
     * 退出登录
     */
    @DeleteMapping("/api/shortLink/v1/user/logout")
    public Result<Void> logout() {
        userService.logout();
        return Results.success();
    }

}