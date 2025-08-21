package com.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortLink.project.dao.entity.ShortLinkOsStatsDO;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

public interface ShortLinkOsStatsMapper extends BaseMapper<ShortLinkOsStatsDO> {

    /**
     * 记录地区访问监控数据
     */
    @Insert("INSERT INTO t_link_os_stats (full_short_url, date, cnt, os, create_time, update_time, del_flag) " +
            "VALUES( #{linkOsStats.fullShortUrl}, #{linkOsStats.date}, #{linkOsStats.cnt}, #{linkOsStats.os}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkOsStats.cnt}, update_time = now();")
    void shortLinkOsState(@Param("linkOsStats") ShortLinkOsStatsDO linkOsStatsDO);

    /**
     * 根据短链接获取指定日期内操作系统监控数据
     */
    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_os_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url,  date, os;")
    List<HashMap<String, Object>> listOsStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);
}
