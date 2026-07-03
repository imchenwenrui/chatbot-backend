package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 消息反馈 DTO(POST /api/conversations/{id}/messages/{messageId}/feedback)。
 * 传 null 表示清除反馈。
 *
 * @author chatbot
 */
@Data
public class MessageFeedbackDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** like / dislike / null */
    @Pattern(regexp = "^(like|dislike)$", message = "feedback 仅支持 like 或 dislike")
    private String feedback;
}
