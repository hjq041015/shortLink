package com.shortLink.admin.common.interceptor;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限拦截器配置
 */
@Configuration
public class AuthInterceptor implements WebMvcConfigurer {

    @Bean
    public UserInfoInterceptor userInfoInterceptor() {
        return new UserInfoInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录接口和注册接口不需要登录，这里不做处理
            // 其他接口都需要登录
            SaRouter.match("/api/shortLink/v1/**", r -> StpUtil.checkLogin());
        })).addPathPatterns("/api/shortLink/v1/**")
          .excludePathPatterns(
              "/api/shortLink/v1/user/login",
              "/api/shortLink/v1/user/register",
              "/api/shortLink/v1/user/has-username"
          );
        
        // 注册用户信息拦截器，用于从JWT中提取用户信息并存入阿里TTL中
        registry.addInterceptor(userInfoInterceptor())
                .addPathPatterns("/api/shortLink/v1/**");
    }
}