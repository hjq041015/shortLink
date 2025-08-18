package com.shortLink.admin.remote;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.dto.req.ShortLinkUpdateReqDTO;
import com.shortLink.admin.dto.resp.ShortLinkGroupCountRespDTO;
import com.shortLink.admin.remote.dto.req.*;
import com.shortLink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.shortLink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */

public interface ShortLinkRemoteService {
   /**
    * 创建短链接
    *
    * @param requestParm 短链接创建请求参数对象，包含生成短链接所需的信息
    * @return ShortLinkCreateRespDTO 短链接创建响应对象，包含生成的短链接信息
    */
   default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParm) {
       String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/shortLink/v1/create", JSON.toJSONString(requestParm));
       return JSON.parseObject(resultBodyStr, new TypeReference<>() {
       });
   }

   /**
    * 分页查询短链接
    *
    * @param requestParam 短链接分页查询请求参数对象，包含分组ID、当前页码和页面大小等信息
    * @return IPage<ShortLinkPageRespDTO> 短链接分页响应数据，包含短链接列表和分页信息
    */
   default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
       Map<String,Object> requestMap = new HashMap<>();
       requestMap.put("gid",requestParam.getGid());
       requestMap.put("current",requestParam.getCurrent());
       requestMap.put("size",requestParam.getSize());
       String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortLink/v1/page", requestMap);
       return JSON.parseObject(resultPageStr,new TypeReference<>() {
       });
   }

   /**
     * 查询短链接组中短链接的数量统计
     *
     * @param requestParm 包含需要统计的短链接组ID列表
     * @return List<ShortLinkGroupCountRespDTO> 短链接组及其对应的短链接数量列表
     */
   default Result<List<ShortLinkGroupCountRespDTO>> listGroupShortLinkCount(List<String> requestParm) {
       Map<String,Object> requstMap = new HashMap<>();
       requstMap.put("requestParm",requestParm);
       String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortLink/v1/count", requstMap);
       return JSON.parseObject(resultPageStr, new TypeReference<>() {
       });
   }
    /**
     * 修改短链接相关信息
     *
     * @param requestParam 修改短链接请求参数
     */
    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/shortLink/v1/update", JSON.toJSONString(requestParam));
    }

    /**
     * 根据 URL 获取标题
     *
     * @param url 目标网站地址
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(@RequestParam("url") String url) {
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortLink/v1/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 保存回收站
     *
     * @param requestParam 请求参数
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/shortLink/v1/recycle-bin/save", JSON.toJSONString(requestParam));
    }

     /**
     * 分页查询回收站短链接
     *
     * @param requestParam 分页短链接请求参数
     * @return 查询短链接响应
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(RecycleBinPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortLink/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 恢复短链接
     *
     * @param requestParam 短链接恢复请求参数
     */
    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/shortLink/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
    }

}
