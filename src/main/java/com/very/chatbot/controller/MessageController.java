package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.MessageFeedbackDTO;
import com.very.chatbot.service.MessageService;
import com.very.chatbot.vo.MessagePageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 消息接口(对应 docs/API文档.md 中 3.7 历史消息 / 3.9 反馈)。
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
     * 历史消息(对应 3.7),按创建时间正序返回某会话下的消息列表。
     *
     * @param conversationId 会话 id(路径参数)
     * @param limit          单页大小,默认 50
     * @return 消息分页 VO(无 nextCursor,简化为单页)
     */
    @GetMapping
    public ApiResponse<MessagePageVO> list(@PathVariable Long conversationId,
                                           @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(messageService.pageMessagesByConversation(conversationId, limit));
    }

    /**
     * 消息反馈(对应 3.9)。
     *
     * {@code feedback = null} 表示清除反馈。
     *
     * @param conversationId 会话 id(路径参数)
     * @param messageId      消息 id(路径参数)
     * @param dto            反馈请求体,feedback 可为 null
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
