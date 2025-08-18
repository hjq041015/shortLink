package com.shortLink.project.service;

public interface UrlTitleService {
     /**
     * 根据 URL 获取对应网站的标题
     */
    String getTitleByUrl(String url);
}
