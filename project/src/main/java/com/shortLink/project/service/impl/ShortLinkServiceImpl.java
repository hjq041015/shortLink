package com.shortLink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shortLink.project.common.convention.exception.ServiceException;
import com.shortLink.project.common.enums.VailDateTypeEnum;
import com.shortLink.project.dao.entity.*;
import com.shortLink.project.dao.mapper.*;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.ShortLinkService;
import com.shortLink.project.toolkit.HashUtil;
import com.shortLink.project.toolkit.LinkUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.shortLink.project.common.constant.RedisKeyConstant.*;
import static com.shortLink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkMapper shortLinkMapper;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final ShortLinkAccessStatsMapper shortLinkAccessStatsMapper;
    private final ShortLinkLocaleStatsMapper shortLinkLocaleStatsMapper;
    private final ShortLinkOsStatsMapper shortLinkOsStatsMapper;
    private final ShortLinkBrowserStatsMapper shortLinkBrowserStatsMapper;
    private final ShortLinkAccessLogsMapper shortLinkAccessLogsMapper;
    private final ShortLinkDeviceStatsMapper shortLinkDeviceStatsMapper;

    @Value("${shortLink.stats.locale.amap-key}")
    private String statsLocaleAmapKey;

    /**
     * 创建短链接
     *
     * @param requestParm 创建短链接的请求参数
     * @return 短链接创建响应结果
     */
    @Transactional
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParm) {
        // 生成短链接后缀
        String shortLinkSuffix = generateSuffix(requestParm);
        String fullShortUrl = StrBuilder.create(requestParm.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        // 构建短链接实体对象
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParm.getDomain())
                .originUrl(requestParm.getOriginUrl())
                .gid(requestParm.getGid())
                .createdType(requestParm.getCreatedType())
                .validDateType(requestParm.getValidDateType())
                .validDate(requestParm.getValidDate())
                .describe(requestParm.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParm.getOriginUrl()))
                .build();
        // 构建短链接跳转实体对象
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParm.getGid())
                .build();
        try{
            // 插入短链接和跳转信息到数据库
            this.baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        }catch (DuplicateKeyException ex) {
                throw new ServiceException(String.format("短链接: %s 生成重复",fullShortUrl));
        }
        // 将短链接和原始链接存入Redis缓存
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),requestParm.getOriginUrl(), LinkUtil.getLinkCacheValidTime(requestParm.getValidDate()),TimeUnit.MILLISECONDS);
        // 将短链接添加到布隆过滤器中，防止缓存穿透
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .gid(requestParm.getGid())
                .fullShortUrl("http://" +shortLinkDO.getFullShortUrl())
                .originUrl(requestParm.getOriginUrl())
                .build();
    }

    /**
     * 分页查询短链接
     *
     * @param requestParam 分页查询参数，包含分页信息和gid
     * @return 短链接分页查询结果
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> page = new Page<>(requestParam.getCurrent(),requestParam.getSize());
        // 构造查询条件：根据gid查询未删除且启用的短链接，并按创建时间倒序排列
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = new LambdaQueryWrapper<>(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid,requestParam.getGid())
                .eq(ShortLinkDO::getDelFlag,0)
                .eq(ShortLinkDO::getEnableStatus,0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        // 执行分页查询，将查询结果填充到 page 对象中
        IPage<ShortLinkDO> resultPage = this.baseMapper.selectPage(page, queryWrapper);
        // 将查询结果从 ShortLinkDO 转换为 ShortLinkPageRespDTO
       return resultPage.convert(each -> {
           ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
           result.setDomain("http://" + result.getDomain());
           return result;
       });

    }

    /**
     * 查询短链接组中短链接的数量统计
     *
     * @param requestParm 包含需要统计的短链接组ID列表
     * @return List<ShortLinkGroupCountRespDTO> 短链接组及其对应的短链接数量列表
     */
    @Override
    public List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParm) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid",requestParm)
                .eq("enable_status",0)
                .eq("del_flag",0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkCounts = shortLinkMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkCounts,ShortLinkGroupCountRespDTO.class);
    }

    /**
     * 更新短链接信息
     *
     * @param requestParm 短链接更新请求参数，包含完整短链接、分组ID、原始链接、有效期等信息
     */
    @Override
    public void updateShortLink(ShortLinkUpdateReqDTO requestParm) {
        // 构造更新条件：完整短链接、分组ID、未删除、启用状态
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParm.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParm.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(Objects.equals(requestParm.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .originUrl(requestParm.getOriginUrl())
                .describe(requestParm.getDescribe())
                .validDate(requestParm.getValidDate())
                .validDateType(requestParm.getValidDateType())
                .build();
            baseMapper.update(shortLinkDO, updateWrapper);
    }

    /**
     * 短链接跳转到原始链接
     *
     * @param shortUrl 短链接
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUrl, HttpServletRequest request, HttpServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUrl;
        // 从Redis中获取原始链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(fullShortUrl,request,response);
            response.sendRedirect(originalLink);
            return;
        }
        // 通过布隆过滤器判断短链接是否存在，防止缓存穿透
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            response.sendRedirect("/page/notfound");
            return;
        }

        // 检查是否是已知的无效短链接
        String nullShortLink  = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY , fullShortUrl));
        if (StrUtil.isNotBlank(nullShortLink)) {
            response.sendRedirect("/page/notfound");
            return;
        }
        // 获取分布式锁，防止缓存击穿
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY , fullShortUrl));
        lock.lock();
        try {
            // 双重检查，再次从Redis中获取原始链接
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY , fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
            shortLinkStats(fullShortUrl,request,response);
            response.sendRedirect(originalLink);
            return;
        }
        // 查询短链接跳转信息
        LambdaQueryWrapper<ShortLinkGotoDO> gotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
        ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(gotoQueryWrapper);
        if (shortLinkGotoDO == null) {
            // 将无效短链接标识存入Redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
            response.sendRedirect("/page/notfound");
            return;
        }
        // 查询短链接详细信息
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl,fullShortUrl)
                .eq(ShortLinkDO::getGid,shortLinkGotoDO.getGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        // 检查短链接是否过期
        if (shortLinkDO == null || shortLinkDO.getValidDate()!= null && shortLinkDO.getValidDate().before(new Date())) {
            // 将过期短链接标识存入Redis，防止缓存穿透
            stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
            response.sendRedirect("/page/notfound");
            return;
        }
        // 根据短链接找到原始链接进行跳转
        stringRedisTemplate.opsForValue().set(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),shortLinkDO.getOriginUrl(), LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()),TimeUnit.MILLISECONDS);
        shortLinkStats(fullShortUrl,request,response);
        response.sendRedirect(shortLinkDO.getOriginUrl());
        }finally {
            // 释放分布式锁
            lock.unlock();
        }

    }


    /**
     * 短链接访问统计
     * <p>
     * 收集并记录短链接的访问统计数据，包括访问时间、用户访问量等指标
     *
     * @param fullShortUrl 完整短链接
     * @param request      HTTP请求对象，包含用户请求信息
     * @param response     HTTP响应对象，用于返回响应数据
     */
    private void shortLinkStats(String fullShortUrl, HttpServletRequest request, HttpServletResponse response) {
        AtomicReference<String> uv = new AtomicReference<>();
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = request.getCookies();
        try{
            // 处理UV统计的Cookie任务
            Runnable addResponseCookieTask = () -> {
                uv.set(UUID.randomUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setPath(StrUtil.sub(fullShortUrl,fullShortUrl.indexOf("/"),fullShortUrl.length()));
                response.addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                stringRedisTemplate.opsForSet().add("short-link:stats:uv" + fullShortUrl,uv.get());
                stringRedisTemplate.expire("short-link:stats:uv" + fullShortUrl,2,TimeUnit.DAYS);
            };
            // 检查请求中是否包含UV统计Cookie
            if (ArrayUtil.isNotEmpty(cookies)) {
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(),"uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            Long uvAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uv" + fullShortUrl, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask);
            }else {
                addResponseCookieTask.run();
            }
            String actualIp = LinkUtil.getIp(request);
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, actualIp);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            // 获取当前小时数和星期数，用于统计分析
            int hour = DateUtil.hour(new Date(),true);
            int weekValue = DateUtil.weekOfMonth(new Date());
            ShortLinkAccessStatsDO accessStatsDO = ShortLinkAccessStatsDO.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .date(new Date())
                    .build();
            shortLinkAccessStatsMapper.shortLinkStats(accessStatsDO);
            Map<String,Object> requestMap = new HashMap<>();
            requestMap.put("key",statsLocaleAmapKey);
            requestMap.put("ip",actualIp);
            String result = HttpUtil.get(AMAP_REMOTE_URL, requestMap);
            JSONObject jsonObject = JSON.parseObject(result);
            String infocode = jsonObject.getString("infocode");
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode,"10000")) {
                String province = jsonObject.getString("province");
                boolean unknownFlag = StrUtil.equals(province,"[]");
                ShortLinkLocaleStatsDO shortLinkLocaleStatsDO = ShortLinkLocaleStatsDO.builder()
                        .province(unknownFlag ? "未知" : province)
                        .city(unknownFlag ? "未知" : jsonObject.getString("city"))
                        .adcode(unknownFlag ? "未知" : jsonObject.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .date(new Date())
                        .build();
                shortLinkLocaleStatsMapper.shortLinkLocaleState(shortLinkLocaleStatsDO);
                String os = LinkUtil.getOs(request);
                ShortLinkOsStatsDO shortLinkOsStatsDO = ShortLinkOsStatsDO.builder()
                        .os(os)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                shortLinkOsStatsMapper.shortLinkOsState(shortLinkOsStatsDO);
                String browser = LinkUtil.getBrowser(request);
                ShortLinkBrowserStatsDO shortLinkBrowserStatsDO = ShortLinkBrowserStatsDO.builder()
                        .browser(browser)
                        .fullShortUrl(fullShortUrl)
                        .cnt(1)
                        .build();
                shortLinkBrowserStatsMapper.shortLinkBrowserState(shortLinkBrowserStatsDO);
                ShortLinkAccessLogsDO shortLinkAccessLogsDO =  ShortLinkAccessLogsDO.builder()
                        .ip(actualIp)
                        .browser(browser)
                        .os(os)
                        .user(uv.get())
                        .fullShortUrl(fullShortUrl)
                        .build();
                shortLinkAccessLogsMapper.insert(shortLinkAccessLogsDO);
                ShortLinkDeviceStatsDO shortLinkDeviceStatsDO = ShortLinkDeviceStatsDO.builder()
                        .device(LinkUtil.getDevice(request))
                        .fullShortUrl(fullShortUrl)
                        .cnt(1)
                        .date(new Date())
                        .build();
                shortLinkDeviceStatsMapper.shortLinkDeviceState(shortLinkDeviceStatsDO);
            }
        }catch (Throwable ex) {
            log.error("短链接访问量统计异常",ex);
        }
    }


    /**
     * 生成短链接后缀
     * <p>
     * 通过哈希算法生成短链接后缀，并通过布隆过滤器避免生成重复的短链接。
     * 如果生成的短链接已存在，则会重新生成，最多尝试10次。
     *
     * @param requestParam 短链接创建请求参数
     * @return 生成的短链接后缀
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shortUri;
        while (true) {
            // 限制生成次数，避免无限循环
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接生成次数过多,请稍后再试");
            }
            String originalUrl = requestParam.getOriginUrl();
            originalUrl += UUID.randomUUID().toString();
            shortUri = HashUtil.hashToBase62(originalUrl);
            // 检查生成的短链接是否已存在，如果不存在则跳出循环
            if(!shortUriCreateCachePenetrationBloomFilter.contains(requestParam.getDomain() + "/" + shortUri)) {
                break;
            }
            customGenerateCount++;
        }
        return shortUri;
    }

    /**
     * 获取网站favicon图标地址
     *
     * @param url 目标网站地址
     * @return favicon图标地址，获取失败时返回null
     */
    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        // 获取HTTP响应状态码
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            // 查找网页中favicon图标链接
            Element first = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (first != null) {
                return first.attr("abs:href");
            }
        }
        return null;

    }
}
