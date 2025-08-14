package com.shortLink.project.dto.req;

import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class ShortLinkPageReqDTO  {

   /**
     * 分组标识
     */
    private String gid;

    /**
     * 当前页
     */
    private long current;

    /**
     * 每页显示数量
     */
    private long size;
}
