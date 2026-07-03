package com.very.chatbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会话表(t_conversation)实体。
 *
 * @author chatbot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_conversation")
public class ConversationEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属项目 id;0=无项目分组 */
    @TableField("project_id")
    private Long projectId;

    /** 会话标题,首版由后端异步生成 */
    @TableField("title")
    private String title;

    /** 所属用户(本期匿名占位) */
    @TableField("user_id")
    private String userId;

    /** Dify 端 conversation_id,仅后端使用,不暴露给前端 */
    @TableField("dify_conv_id")
    private String difyConvId;

    /** 是否归档 0=否 1=是(对应 UI 隐藏/恢复) */
    @TableField("archived")
    private Integer archived;
}
