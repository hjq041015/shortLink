package com.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.shortLink.project.dao.entity.ShortLinkAccessStatsDO;
import com.shortLink.project.dao.entity.ShortLinkDeviceStatsDO;
import com.shortLink.project.dao.entity.ShortLinkLocaleStatsDO;
import com.shortLink.project.dao.entity.ShortLinkNetworkStatsDO;
import com.shortLink.project.dao.mapper.*;
import com.shortLink.project.dto.req.ShortLinkStatsReqDTO;
import com.shortLink.project.dto.resp.*;
import com.shortLink.project.service.ShortLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class ShortLinkStatsServiceImpl implements ShortLinkStatsService {

    private final ShortLinkAccessStatsMapper shortLinkAccessStatsMapper;
    private final ShortLinkLocaleStatsMapper shortLinkLocaleStatsMapper;
    private final ShortLinkOsStatsMapper shortLinkOsStatsMapper;
    private final ShortLinkBrowserStatsMapper shortLinkBrowserStatsMapper;
    private final ShortLinkAccessLogsMapper shortLinkAccessLogsMapper;
    private final ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;
    private final ShortLinkNetWorkStatsMapper shortLinkNetWorkStatsMapper;


    /**
      获取单个短链接的统计信息

      @param requestParam 短链接统计请求参数
     * @return 短链接统计响应数据
     */
    @Override
    public ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        // 基础访问详情
        List<ShortLinkAccessStatsDO> listStatsByShortLink = shortLinkAccessStatsMapper.listStatsByShortLink(requestParam);
        // 地区访问详情
        List<ShortLinkStatsLocaleCNRespDTO> localeCnStats = new ArrayList<>();
        List<ShortLinkLocaleStatsDO> listLocaleStatsByShortLink = shortLinkLocaleStatsMapper.listLocaleByShortLink(requestParam);
        int localeSum = listLocaleStatsByShortLink.stream().mapToInt(ShortLinkLocaleStatsDO::getCnt).sum();
        listLocaleStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / localeSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsLocaleCNRespDTO localeCNRespDTO = ShortLinkStatsLocaleCNRespDTO.builder()
                    .cnt(each.getCnt())
                    .locale(each.getProvince())
                    .ratio(actualRatio)
                    .build();
             localeCnStats.add(localeCNRespDTO);
        });
        // 每小时的访问详情
        List<Integer> hourStats = new ArrayList<>();
        List<ShortLinkAccessStatsDO> listHourStatsByShortLink = shortLinkAccessStatsMapper.listHourStatsByShortLink(requestParam);
        for (int i = 0; i < 24; i++) {
            AtomicInteger hour = new AtomicInteger(i);
            Integer hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(hour.get(), each.getHour()))
                    .findFirst()
                    .map(ShortLinkAccessStatsDO::getPv)
                    .orElse(0);
            hourStats.add(hourCnt);
        }
        // 每周的访问详情
        List<Integer> weekStats = new ArrayList<>();
        List<ShortLinkAccessStatsDO> listWeekStatsByShortLink = shortLinkAccessStatsMapper.listWeekdayStatsByShortLink(requestParam);
        for (int i = 1; i < 7; i++) {
            AtomicInteger week = new AtomicInteger(i);
            Integer weekCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(week.get(), each.getWeekday()))
                    .findFirst()
                    .map(ShortLinkAccessStatsDO::getPv)
                    .orElse(0);
            weekStats.add(weekCnt);
        }
        // 高频IP访问详情
        List<ShortLinkStatsTopIpRespDTO> topIpStats = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpShortLink = shortLinkAccessLogsMapper.listTopIpByShortLink(requestParam);
        listTopIpShortLink.forEach(each-> {
            ShortLinkStatsTopIpRespDTO shortLinkStatsTopIpRespDTO = ShortLinkStatsTopIpRespDTO.builder().
                    ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .build();
            topIpStats.add(shortLinkStatsTopIpRespDTO);
        });
        // 浏览器访问详情
        List<ShortLinkStatsBrowserRespDTO> browserStats = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatsByShortLink = shortLinkBrowserStatsMapper.listBrowserStatsByShortLink(requestParam);
        int browserSum = listBrowserStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listBrowserStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsBrowserRespDTO browserRespDTO = ShortLinkStatsBrowserRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserStats.add(browserRespDTO);
        });
        // 操作系统访问详情
        List<ShortLinkStatsOsRespDTO> osStats = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatsByShortLink = shortLinkOsStatsMapper.listOsStatsByShortLink(requestParam);
        int osSum = listOsStatsByShortLink.stream()
                .mapToInt(each -> Integer.parseInt(each.get("count").toString()))
                .sum();
        listOsStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("count").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsOsRespDTO osRespDTO = ShortLinkStatsOsRespDTO.builder()
                    .cnt(Integer.parseInt(each.get("count").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osStats.add(osRespDTO);
        });
        // 访问网络类型详情
        List<ShortLinkStatsNetworkRespDTO> networkStats = new ArrayList<>();
        List<ShortLinkNetworkStatsDO> listNetworkStatsByShortLink = shortLinkNetWorkStatsMapper.listNetworkStatsByShortLink(requestParam);
        int networkSum = listNetworkStatsByShortLink.stream()
                .mapToInt(ShortLinkNetworkStatsDO::getCnt)
                .sum();
        listNetworkStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsNetworkRespDTO networkRespDTO = ShortLinkStatsNetworkRespDTO.builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkStats.add(networkRespDTO);
        });
        // 访问设备类型详情
        List<ShortLinkStatsDeviceRespDTO> deviceStats = new ArrayList<>();
        List<ShortLinkDeviceStatsDO> listDeviceStatsByShortLink = shortLinkDeviceStatsMapper.listDeviceStatsByShortLink(requestParam);
        int deviceSum = listDeviceStatsByShortLink.stream()
                .mapToInt(ShortLinkDeviceStatsDO::getCnt)
                .sum();
        listDeviceStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            ShortLinkStatsDeviceRespDTO deviceRespDTO = ShortLinkStatsDeviceRespDTO.builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceStats.add(deviceRespDTO);
        });
         // 访客访问类型详情
        List<ShortLinkStatsUvRespDTO> uvTypeStats = new ArrayList<>();
        HashMap<String, Object> findUvTypeByShortLink = shortLinkAccessLogsMapper.findUvTypeCntByShortLink(requestParam);
        int oldUserCnt = Integer.parseInt(findUvTypeByShortLink.get("oldUserCnt").toString());
        int newUserCnt = Integer.parseInt(findUvTypeByShortLink.get("newUserCnt").toString());
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        ShortLinkStatsUvRespDTO newUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeStats.add(newUvRespDTO);
        ShortLinkStatsUvRespDTO oldUvRespDTO = ShortLinkStatsUvRespDTO.builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeStats.add(oldUvRespDTO);
         return ShortLinkStatsRespDTO.builder()
                .daily(BeanUtil.copyToList(listStatsByShortLink, ShortLinkStatsAccessDailyRespDTO.class))
                .localeCnStats(localeCnStats)
                .hourStats(hourStats)
                .topIpStats(topIpStats)
                .weekdayStats(weekStats)
                .browserStats(browserStats)
                .osStats(osStats)
                .uvTypeStats(uvTypeStats)
                .deviceStats(deviceStats)
                .networkStats(networkStats)
                .build();
    }
}
