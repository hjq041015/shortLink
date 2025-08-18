package com.shortLink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.remote.ShortLinkRemoteService;
import com.shortLink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.shortLink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import com.shortLink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import com.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.shortLink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {
     /**
     * 后续重构为 SpringCloud Feign 调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/shortLink/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 回收站分页查询
     */
    @GetMapping("/api/shortLink/admin/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(RecycleBinPageReqDTO requestParm) {
        return recycleBinService.pageRecycleBinShortLink(requestParm);
    }

    /**
     * 从回收站恢复短链接
     */
    @PostMapping("/api/shortLink/admin/v1/recycle-bin/recover")
    public Result<Void> recover(@RequestBody RecycleBinRecoverReqDTO requestParm) {
        shortLinkRemoteService.recoverRecycleBin(requestParm);
        return Results.success();
    }

}
