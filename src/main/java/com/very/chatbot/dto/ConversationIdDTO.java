package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 会话 id 请求 DTO
 * 用于 POST /api/conversations/{conversationId}/* 这类仅靠路径参数无法表达全部入参、或需要在请求体里再次携带会话 id 的接口。
 *
 * @author chatbot
 */
@Data
public class ConversationIdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话 id,必填 */
    @NotNull(message = "会话 id 不能为空")
    private Long conversationId;
}
