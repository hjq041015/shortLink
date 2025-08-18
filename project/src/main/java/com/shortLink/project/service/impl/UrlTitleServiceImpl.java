package com.shortLink.project.service.impl;

import com.shortLink.project.service.UrlTitleService;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class UrlTitleServiceImpl implements UrlTitleService {
    /**
     * 根据 URL 获取对应网站的标题
     *
     * @param url 目标网站地址
     * @return 网站标题，获取失败时返回错误提示信息
     */
    @SneakyThrows
    @Override
    public String getTitleByUrl(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        // 获取HTTP响应状态码
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Document document = Jsoup.connect(url).get();
            return document.title();
        }
        return  "Error while fetching title.";
    }
}
