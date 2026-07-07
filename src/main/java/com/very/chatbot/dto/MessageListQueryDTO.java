package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 历史消息查询条件 DTO 对应接口 POST /api/conversations/{conversationId}/messages/list。
 *
 * @author chatbot
 */
@Data
public class MessageListQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单页大小 默认 50 范围 1~500 */
    @Min(value = 1, message = "limit 不能小于 1")
    @Max(value = 500, message = "limit 不能大于 500")
    private Integer limit;
}
