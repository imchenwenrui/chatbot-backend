package com.very.chatbot.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息响应 VO。
 *
 * @author chatbot
 */
@Data
public class MessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    private String feedback;
    private Boolean incomplete;
    private LocalDateTime createdAt;
}
