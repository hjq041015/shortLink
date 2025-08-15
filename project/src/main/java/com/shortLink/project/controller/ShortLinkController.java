package com.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.project.common.convention.result.Result;
import com.shortLink.project.common.convention.result.Results;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParm) {
        return Results.success(shortLinkService.createShortLink(requestParm));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     *  查询短链接分组中的短链接数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountRespDTO>> listGroupShortLinkCount(@RequestParam("requestParm") List<String> requestParm) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParm));
    }
}
