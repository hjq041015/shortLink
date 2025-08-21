package com.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortLink.project.dao.entity.ShortLinkLocaleStatsDO;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShortLinkLocaleStatsMapper extends BaseMapper<ShortLinkLocaleStatsDO> {

    /**
     * 记录地区访问监控数据
     */
    @Insert("INSERT INTO t_link_locale_stats (full_short_url,  date, cnt, country, province, city, adcode, create_time, update_time, del_flag) " +
            "VALUES( #{linkLocaleStats.fullShortUrl},  #{linkLocaleStats.date}, #{linkLocaleStats.cnt}, #{linkLocaleStats.country}, #{linkLocaleStats.province}, #{linkLocaleStats.city}, #{linkLocaleStats.adcode}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkLocaleStats.cnt}, update_time = now();")
    void shortLinkLocaleState(@Param("linkLocaleStats") ShortLinkLocaleStatsDO linkLocaleStatsDO);


     /**
     * 根据短链接获取指定日期内基础监控数据
     */
    @Select("SELECT " +
            "    province, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_locale_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url,  province;")
    List<ShortLinkLocaleStatsDO> listLocaleByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

}
