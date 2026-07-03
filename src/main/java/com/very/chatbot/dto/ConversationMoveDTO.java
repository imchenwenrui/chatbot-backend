package com.very.chatbot.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 移动会话到指定项目请求 DTO(PATCH /api/conversations/{id}/project)。
 * projectId 为 null 表示移到「无项目」分组。
 *
 * @author chatbot
 */
@Data
public class ConversationMoveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 目标项目 id;null 表示无项目 */
    private Long projectId;
}
