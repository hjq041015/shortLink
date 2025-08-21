package com.shortLink.admin.remote.dto.req;

import lombok.Data;

/**
 * 短链接监控请求参数
 */
@Data
public class ShortLinkStatsReqDTO {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
