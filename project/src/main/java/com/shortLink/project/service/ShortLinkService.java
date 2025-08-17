package com.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dto.req.ShortLinkCreateReqDTO;
import com.shortLink.project.dto.req.ShortLinkPageReqDTO;
import com.shortLink.project.dto.req.ShortLinkUpdateReqDTO;
import com.shortLink.project.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.project.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 短链接服务接口
 */
public interface ShortLinkService extends IService<ShortLinkDO> {


    /**
     * 创建短链接
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParm);

    /**
     *  短链接分页查询
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);


    /**
     *  查询短链接分组中的短链接数量
     */
    List<ShortLinkGroupCountRespDTO> listGroupShortLinkCount(List<String> requestParm);

    /**
     *  修改短链接
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParm);

    /**
     * 短链接的跳转
     */
    void restoreUrl(String shortUrl, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
