package com.shortLink.project.dao.mapper;

import com.shortLink.project.dao.entity.ShortLinkBrowserStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkBrowserStatsMapper {

    /**
     * 记录浏览器访问监控数据
     */
    @Insert("INSERT INTO t_link_browser_stats (full_short_url, cnt, browser, create_time, update_time, del_flag) " +
            "VALUES( #{linkBrowserStats.fullShortUrl},  #{linkBrowserStats.cnt}, #{linkBrowserStats.browser}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkBrowserStats.cnt}, update_time = now() ;")
    void shortLinkBrowserState(@Param("linkBrowserStats") ShortLinkBrowserStatsDO linkBrowserStatsDO);
}
