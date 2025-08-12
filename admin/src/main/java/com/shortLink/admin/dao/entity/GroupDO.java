package com.shortLink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shortLink.admin.common.database.BaseDO;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_group")
public class GroupDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

}