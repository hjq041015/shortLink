package com.shortLink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 短链接服务接口
 */
public interface ShortLinkService extends IService<ShortLinkDO> {


    /**
     * 创建短链接
     */
    ShortLinkCreateRespDTO crateShortLink(ShortLinkCreateReqDTO requestParm);
}
