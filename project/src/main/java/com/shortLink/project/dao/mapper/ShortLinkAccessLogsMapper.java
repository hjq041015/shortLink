package com.shortLink.project.dao.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortLink.project.dao.entity.ShortLinkAccessLogsDO;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;


public interface ShortLinkAccessLogsMapper extends BaseMapper<ShortLinkAccessLogsDO> {

    /**
     * 根据短链接获取指定日期内高频Ip数据
     */
    @Select("SELECT " +
        "    ip, " +
        "    COUNT(ip) AS count " +
        "FROM " +
        "    t_link_access_logs " +
        "WHERE " +
        "    full_short_url = #{param.fullShortUrl} " +
        "    AND create_time BETWEEN CONCAT(#{param.startDate}, ' 00:00:00') " +
        "                     AND CONCAT(#{param.endDate}, ' 23:59:59') " +
        "GROUP BY " +
        "    full_short_url, ip " +
        "ORDER BY " +
        "    count DESC " +
        "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    @Select("SELECT " +
            "    SUM(old_user) AS oldUserCnt, " +
            "    SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{param.startDate} AND MAX(create_time) <= #{param.endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        full_short_url = #{param.fullShortUrl} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
