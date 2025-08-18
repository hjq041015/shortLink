package com.shortLink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shortLink.project.dao.entity.ShortLinkDO;
import com.shortLink.project.dto.req.RecycleBinSaveReqDTO;

public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     */
    void save(RecycleBinSaveReqDTO requestParm);
}
