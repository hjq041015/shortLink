package com.shortLink.project.dao.mapper;

import com.shortLink.project.dao.entity.ShortLinkBrowserStatsDO;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

public interface ShortLinkBrowserStatsMapper {

    /**
     * 记录浏览器访问监控数据
     */
    @Insert("INSERT INTO t_link_browser_stats (full_short_url, date,cnt, browser, create_time, update_time, del_flag) " +
            "VALUES( #{linkBrowserStats.fullShortUrl},#{linkBrowserStats.date},  #{linkBrowserStats.cnt}, #{linkBrowserStats.browser}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkBrowserStats.cnt}, update_time = now() ;")
    void shortLinkBrowserState(@Param("linkBrowserStats") ShortLinkBrowserStatsDO linkBrowserStatsDO);


     /**
     * 根据短链接获取指定日期内浏览器监控数据
     */
    @Select("SELECT " +
            "    browser, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_browser_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, date, browser;")
    List<HashMap<String, Object>> listBrowserStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
