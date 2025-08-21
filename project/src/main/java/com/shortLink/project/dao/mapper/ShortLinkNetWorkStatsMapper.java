package com.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortLink.project.dao.entity.ShortLinkNetworkStatsDO;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ShortLinkNetWorkStatsMapper extends BaseMapper<ShortLinkNetworkStatsDO> {

    /**
     * 记录访问设备监控数据
     */
    @Insert("INSERT INTO t_link_network_stats (full_short_url, date, cnt, network, create_time, update_time, del_flag) " +
            "VALUES( #{linkNetworkStats.fullShortUrl}, #{linkNetworkStats.date}, #{linkNetworkStats.cnt}, #{linkNetworkStats.network}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkNetworkStats.cnt},update_time = now();")
    void shortLinkNetworkState(@Param("linkNetworkStats") ShortLinkNetworkStatsDO linkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    @Select("SELECT " +
            "    network, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_network_stats " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    full_short_url, network;")
    List<ShortLinkNetworkStatsDO> listNetworkStatsByShortLink(@Param("param") ShortLinkStatsReqDTO requestParam);

}
