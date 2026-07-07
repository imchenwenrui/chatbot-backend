package com.very.chatbot.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新建会话请求 DTO 对应接口 POST /api/conversations。
 * projectId 可空,空表示「无项目」分组。
 *
 * @author chatbot
 */
@Data
public class ConversationAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 所属项目 id;不传或 null 表示无项目分组 */
    private Long projectId;
}
