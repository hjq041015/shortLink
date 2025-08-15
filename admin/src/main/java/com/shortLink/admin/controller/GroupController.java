package com.shortLink.admin.controller;

import com.shortLink.admin.common.convention.result.Result;
import com.shortLink.admin.common.convention.result.Results;
import com.shortLink.admin.dto.req.GroupSortReqDTO;
import com.shortLink.admin.dto.req.GroupUpdateReqDTO;
import com.shortLink.admin.dto.req.ShortLinkGroupAddReqDTO;
import com.shortLink.admin.dto.resp.GroupRespDTO;
import com.shortLink.admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/api/shortLink/admin/v1/group/add")
    public Result<Void> addGroup(@RequestBody ShortLinkGroupAddReqDTO requestParm) {
        groupService.addGroup(requestParm.getName());
       return Results.success();
    }

    /**
     * 查询分组
     */
    @GetMapping("/api/shortLink/admin/v1/group/query")
    public Result<List<GroupRespDTO>> queryGroup(){
        return Results.success(groupService.queryGroup());
    }

    /**
     *  修改分组
     */
    @PutMapping("/api/shortLink/admin/v1/group/update")
    public Result<Void> updateGroup(@RequestBody GroupUpdateReqDTO requestParm) {
        groupService.updateGroup(requestParm);
        return Results.success();
    }

    /**
     *  删除分组
     */
    @DeleteMapping("/api/shortLink/admin/v1/group/delete")
    public Result<Void> deleteGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();
    }

    /**
     *  排序分组
     */
    @PostMapping("/api/shortLink/admin/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<GroupSortReqDTO> requestParm) {
        groupService.sortGroup(requestParm);
        return Results.success();
    }




}
