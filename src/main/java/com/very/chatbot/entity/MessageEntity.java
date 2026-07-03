package com.very.chatbot.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息表(t_message)实体。
 *
 * @author chatbot
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_message")
public class MessageEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 所属会话 id */
    @TableField("conversation_id")
    private Long conversationId;

    /** user / assistant / system */
    @TableField("role")
    private String role;

    /** 消息最终内容(流式落地后可空) */
    @TableField("content")
    private String content;

    /** like / dislike / NULL */
    @TableField("feedback")
    private String feedback;

    /** 是否被中断 0=否 1=是 */
    @TableField("incomplete")
    private Integer incomplete;

    /** Dify 端 message_id,仅后端使用,不暴露给前端 */
    @TableField("dify_message_id")
    private String difyMessageId;
}
