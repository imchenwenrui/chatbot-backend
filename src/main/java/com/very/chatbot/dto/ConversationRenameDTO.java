package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 重命名会话请求 DTO(PATCH /api/conversations/{id})。
 *
 * @author chatbot
 */
@Data
public class ConversationRenameDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话标题,1~255 字符 */
    @NotBlank(message = "会话标题不能为空")
    @Size(min = 1, max = 255, message = "会话标题长度 1~255 字符")
    private String title;
}
