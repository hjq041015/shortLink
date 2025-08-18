package com.shortLink.project.controller;

import com.shortLink.project.common.convention.result.Result;
import com.shortLink.project.common.convention.result.Results;
import com.shortLink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URl 标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    private final UrlTitleService urlTitleService;


     /**
     * 根据 URL 获取对应网站的标题
     */
    @GetMapping("/api/shortLink/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));

    }
}
