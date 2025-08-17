package com.shortLink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.shortLink.project.common.constant.RedisKeyConstant.DEFAULT_CACHE_VALID_TIME;

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

}
