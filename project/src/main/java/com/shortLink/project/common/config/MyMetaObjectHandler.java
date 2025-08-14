package com.shortLink.project.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 元数据对象处理器
 * 用于自动填充实体类的公共字段，如创建时间、更新时间、逻辑删除标识等。
 * 通过实现 MetaObjectHandler 接口，在插入和更新数据时自动设置相关字段，减少重复代码。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入数据时自动填充字段
     * @param metaObject 元数据对象，包含了当前操作的实体信息
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", Date::new, Date.class);
        this.strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
        this.strictInsertFill(metaObject, "delFlag", ()-> 0 , Integer.class);
    }

    /**
     * 更新数据时自动填充字段
     * @param metaObject 元数据对象，包含了当前操作的实体信息
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
    }
}
