package com.shortLink.admin.toolkit;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 用户上下文信息持有类，使用阿里TTL实现线程间上下文传递
 */
public final class UserContextHolder {

    // 使用阿里TTL实现线程间上下文传递
    private static final ThreadLocal<Long> USER_ID = new TransmittableThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new TransmittableThreadLocal<>();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        return USERNAME.get();
    }


    /**
     * 清除当前线程的用户上下文信息
     */
    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }

    /**
     * 私有构造方法，防止实例化
     */
    private UserContextHolder() {
    }
}