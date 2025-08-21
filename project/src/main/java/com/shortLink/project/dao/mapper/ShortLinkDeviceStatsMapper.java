package com.shortLink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shortLink.project.dao.entity.ShortLinkDeviceStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface ShortLinkDeviceStatsMapper extends BaseMapper<ShortLinkDeviceStatsMapper> {
     /**
     * 记录访问设备监控数据
     */
    @Insert("INSERT INTO t_link_device_stats (full_short_url,  date, cnt, device, create_time, update_time, del_flag) " +
            "VALUES( #{linkDeviceStats.fullShortUrl},  #{linkDeviceStats.date}, #{linkDeviceStats.cnt}, #{linkDeviceStats.device}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkDeviceStats.cnt}, update_time = now();")
    void shortLinkDeviceState(@Param("linkDeviceStats") ShortLinkDeviceStatsDO linkDeviceStatsDO);
}
