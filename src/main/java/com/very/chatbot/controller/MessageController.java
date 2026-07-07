package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.MessageFeedbackDTO;
import com.very.chatbot.dto.MessageListQueryDTO;
import com.very.chatbot.service.MessageService;
import com.very.chatbot.vo.MessagePageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 消息接口,对应 API 文档第 3.7 节历史消息与第 3.9 节反馈。
 *
 * 全部接口强制走 POST。
 *
 * @author chatbot
 */
@Slf4j
@RestController
@RequestMapping("/api/conversations/{conversationId}/messages")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 历史消息,对应接口第 3.7 节。按创建时间正序返回某会话下的消息列表。
     *
     * @param conversationId 会话 id 路径参数
     * @param query          查询条件 仅包含 limit 字段,可空
     * @return 消息分页 VO 简化为单页 不暴露 nextCursor
     */
    @PostMapping("/list")
    public ApiResponse<MessagePageVO> list(@PathVariable Long conversationId,
                                           @Valid @RequestBody(required = false) MessageListQueryDTO query) {
        Integer limit = query == null ? null : query.getLimit();
        return ApiResponse.ok(messageService.pageMessagesByConversation(conversationId, limit));
    }

    /**
     * 消息反馈,对应接口第 3.9 节。
     *
     * feedback = null 表示清除反馈。
     *
     * @param conversationId 会话 id 路径参数
     * @param messageId      消息 id 路径参数
     * @param dto            反馈请求体 feedback 可为 null
     * @return 空响应
     */
    @PostMapping("/{messageId}/feedback")
    public ApiResponse<Void> feedback(@PathVariable Long conversationId,
                                      @PathVariable Long messageId,
                                      @Valid @RequestBody(required = false) MessageFeedbackDTO dto) {
        String value = dto == null ? null : dto.getFeedback();
        messageService.setFeedback(conversationId, messageId, value);
        return ApiResponse.ok();
    }
}
