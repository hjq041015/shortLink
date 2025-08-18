package com.shortLink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.remote.dto.req.RecycleBinPageReqDTO;
import com.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站接口层
 */
public interface RecycleBinService {
    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(RecycleBinPageReqDTO requestParam);
}
