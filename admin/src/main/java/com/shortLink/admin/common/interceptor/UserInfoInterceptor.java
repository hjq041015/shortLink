package com.shortLink.admin.common.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.shortLink.admin.dao.entity.UserDO;
import com.shortLink.admin.toolkit.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户信息拦截器，从JWT中提取用户信息并存入阿里TTL中
 */
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 判断用户是否已登录
            if (StpUtil.isLogin()) {
                // 从Sa-Token的Session中获取用户信息
                String userInfoStr = (String) StpUtil.getSession().get("userInfo");
                if (userInfoStr != null) {
                    UserDO userDO = JSON.parseObject(userInfoStr, UserDO.class);
                    // 将用户信息存入阿里TTL中
                    UserContextHolder.setUserId(userDO.getId());
                    UserContextHolder.setUsername(userDO.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("从JWT中提取用户信息失败", e);
            // 异常不影响请求继续执行
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清除当前线程的用户上下文信息
        UserContextHolder.clear();
    }
}