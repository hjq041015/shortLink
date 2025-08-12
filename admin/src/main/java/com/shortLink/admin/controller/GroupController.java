package com.shortLink.admin.controller;

import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.shortLink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分组管理控制器
 */
@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    /**
     * 增加分组
     */
    @PostMapping("/api/shortLink/v1/group/add")
    public Result<Void> addGroup(@RequestBody ShortLinkGroupSaveReqDTO requestParm) {
        groupService.addGroup(requestParm.getName());
       return Results.success();
    }

    /**
     * 查询分组
     */
    @GetMapping("/api/shortLink/v1/group/query")
    public Result<List<ShortLinkGroupSaveReqDTO>> queryGroup(){
        return Results.success(groupService.queryGroup());
    }




}

