package com.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.project.common.convention.result.Result;
import com.shortLink.project.common.convention.result.Results;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortLink/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParm) {
        return Results.success(shortLinkService.createShortLink(requestParm));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortLink/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     *  查询短链接分组中的短链接数量
     */
    @GetMapping("/api/shortLink/v1/count")
    public Result<List<ShortLinkGroupCountRespDTO>> listGroupShortLinkCount(@RequestParam("requestParm") List<String> requestParm) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParm));
    }

    /**
     *  修改短链接
     */
    @PostMapping("/api/shortLink/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParm) {
        shortLinkService.updateShortLink(requestParm);
        return Results.success();
    }

    /**
     *  短链接的跳转
     */
    @SneakyThrows
    @GetMapping("/{short-url}")
    public void  restoreUrl(@PathVariable("short-url") String shortUrl, HttpServletRequest request, HttpServletResponse response) {
        shortLinkService.restoreUrl(shortUrl,request,response);
    }
}
