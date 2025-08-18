package com.shortLink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dto.req.RecycleBinPageReqDTO;
import com.shortLink.project.dto.req.RecycleBinRecoverReqDTO;
import com.shortLink.project.dto.req.RecycleBinSaveReqDTO;
import com.shortLink.project.dto.resp.ShortLinkPageRespDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     */
    void save(RecycleBinSaveReqDTO requestParm);

    /**
     * 分页查询回收站短链接
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(RecycleBinPageReqDTO requestParm);

    /**
     *  恢复短链接
     */
    void recover(RecycleBinRecoverReqDTO requestParm);
}
