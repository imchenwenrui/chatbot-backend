package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 发送消息请求 DTO,对应接口 POST /api/conversations/{conversationId}/messages,SSE。
 *
 * 对应 API 文档第 5.1 节。
 *
 * @author chatbot
 */
@Data
public class MessageSendDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 消息内容,1~4000 字符(超出返回 1001) */
    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 4000, message = "消息内容长度需在 1~4000 字符之间")
    private String content;
}
