package com.shortLink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.project.common.convention.result.Result;
import com.shortLink.project.common.convention.result.Results;
import com.shortLink.project.dto.req.RecycleBinPageReqDTO;
import com.shortLink.project.dto.req.RecycleBinRecoverReqDTO;
import com.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
    private final RecycleBinService recycleBinService;



    /**
     * 保存回收站
     */
    @PostMapping("/api/shortLink/v1/recycle-bin/save")
    public Result<Void> save(@RequestBody RecycleBinSaveReqDTO requestParm) {
        recycleBinService.save(requestParm);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/shortLink/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(RecycleBinPageReqDTO requestParm) {
        return Results.success(recycleBinService.pageShortLink(requestParm));
    }

    /**
     *  恢复短链接
     */
    @PostMapping("/api/shortLink/v1/recycle-bin/recover")
    public Result<Void> recover(@RequestBody RecycleBinRecoverReqDTO requestParm) {
        recycleBinService.recover(requestParm);
        return Results.success();
    }
}
