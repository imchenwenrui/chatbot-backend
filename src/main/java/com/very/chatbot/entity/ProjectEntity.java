package com.very.chatbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目表(t_project)实体。
 *
 * @author chatbot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_project")
public class ProjectEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 项目名称,1~50 字符 */
    @TableField("name")
    private String name;

    /** 项目描述,0~200 字符 */
    @TableField("description")
    private String description;

    /** 所属用户(本期匿名占位) */
    @TableField("user_id")
    private String userId;
}
