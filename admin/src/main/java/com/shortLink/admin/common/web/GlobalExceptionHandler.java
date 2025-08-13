package com.shortLink.admin.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.shortLink.admin.common.convention.errorcode.BaseErrorCode;
import com.shortLink.admin.common.convention.exception.AbstractException;
import com.shortLink.admin.common.convention.exception.ClientException;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

/**
 *  全局异常拦截器
 */
@Component
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 拦截参数验证异常
     */
    @SneakyThrows
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<?> validaExceptionHandler(HttpServletRequest request, MethodArgumentNotValidException ex) {
        // 获取参数校验结果
        BindingResult bindingResult = ex.getBindingResult();
        // 获取第一个字段错误
        FieldError firstFileError = CollectionUtil.getFirst(bindingResult.getFieldErrors());
        // 提取错误信息
        String exceptionStr = Optional.ofNullable(firstFileError)
                .map(FieldError::getDefaultMessage)
                .orElse(StrUtil.EMPTY);
        log.error("[{}] {} [ex] {}", request.getMethod(), getUrl(request), exceptionStr);
        // 返回统一失败响应
        return Results.failure(BaseErrorCode.CLIENT_ERROR.code(), exceptionStr);
    }

     /**
     * 拦截应用内抛出的异常
     */
     @ExceptionHandler(value = AbstractException.class)
     public Result<?> abstractException(HttpServletRequest request, AbstractException ex) {
         // 判断是否有原始的异常
         if (ex.getCause() != null) {
             log.error("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString(), ex.getCause());
             return Results.failure(ex);
         }
            log.error("[{}] {} [ex] {}", request.getMethod(), request.getRequestURL().toString(), ex.toString());
        return Results.failure(ex);
    }

     /**
     * 拦截未捕获异常
     */
    @ExceptionHandler(value = Throwable.class)
    public Result<?> defaultErrorHandler(HttpServletRequest request, Throwable throwable) {
        // 记录未捕获异常日志
        log.error("[{}] {} ", request.getMethod(), getUrl(request), throwable);
        // 返回通用失败响应
        return Results.failure();
    }

    /**
     * 处理Sa-Token未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("用户未登录: {}", e.getMessage());
        throw new ClientException("用户未登录", BaseErrorCode.CLIENT_ERROR);
    }

    /**
     * 处理Sa-Token无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("用户无权限: {}", e.getMessage());
        throw new ClientException("用户无权限", BaseErrorCode.CLIENT_ERROR);
    }

    /**
     * 处理Sa-Token无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("用户无角色: {}", e.getMessage());
        throw new ClientException("用户无角色", BaseErrorCode.CLIENT_ERROR);
    }
    
    /**
     * 获取请求完整 URL（包含 queryString）。
     * @param request 当前请求对象
     * @return 完整 URL 字符串
     */
    private String getUrl(HttpServletRequest request) {
        // 获取请求参数字符串
        String queryString = request.getQueryString();
        // 拼接完整 URL
        if (StringUtils.hasLength(queryString)) {
            return request.getRequestURL().toString();
        }
        return request.getRequestURL().toString() + "?" + queryString;
    }
}
