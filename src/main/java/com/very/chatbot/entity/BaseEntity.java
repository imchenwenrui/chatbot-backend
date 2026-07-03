package com.very.chatbot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类。
 *
 * 所有实体继承本类,统一管理主键、创建/更新时间、逻辑删除字段。
 *
 * 主键:雪花 Long,与 CLAUDE.md 的 IdType.ASSIGN_ID 对齐
 * createTime / updateTime:由 MetaObjectHandler 自动填充
 * deleted:逻辑删除(MP 自动过滤)
 *
 * @author chatbot
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键,雪花 Long。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间,由 MetaObjectHandler 在 insert 时自动填充。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间,由 MetaObjectHandler 在 insert / update 时自动填充。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记(0=否,1=是),MP 自动过滤,默认 0。
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
