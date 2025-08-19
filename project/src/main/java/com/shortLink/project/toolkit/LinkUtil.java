package com.shortLink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

import static com.shortLink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

     /**
     * 获取短链接缓存有效期时间
     *
     * @param valiDate 有效期时间
     * @return 有限期时间戳
     */
    public static long getLinkCacheValidTime(Date valiDate) {
        return Optional.ofNullable(valiDate)
                .map(each -> DateUtil.between(new Date(),each, DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }


     /**
     * 获取客户端IP地址
     * <p>
     * 通过从HTTP请求头中获取IP地址信息，依次尝试多种方式获取真实客户端IP地址，
     * 包括代理服务器转发的IP地址和直接连接的IP地址
     *
     * @param request HTTP请求对象，用于获取请求头信息
     * @return 客户端IP地址字符串
     */
public static String getIp(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");

    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
        ipAddress = request.getRemoteAddr();
    }

    return ipAddress;
}

}
