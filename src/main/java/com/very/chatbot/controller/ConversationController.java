package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.ConversationAddDTO;
import com.very.chatbot.dto.ConversationMoveDTO;
import com.very.chatbot.dto.ConversationQueryDTO;
import com.very.chatbot.dto.ConversationRenameDTO;
import com.very.chatbot.dto.MessageSendDTO;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.service.ConversationService;
import com.very.chatbot.vo.ConversationPageVO;
import com.very.chatbot.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;

/**
 * 会话接口,对应 API 文档第 3 章,不含消息相关。
 *
 * 全部接口强制走 POST,请求体统一 JSON。资源 id 通过路径参数表达,业务字段通过 DTO 请求体表达。
 *
 * 流式发送消息 SSE 与停止生成属于第 5.1 与 5.2 节,同样强制 POST,一并暴露。
 *
 * @author chatbot
 */
@Slf4j
@RestController
@RequestMapping("/api/conversations")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 新建会话,对应接口第 3.1 节。
     *
     * @param dto 新建请求体 可空 projectId 为 null 表示「无项目」分组
     * @return 新建后的会话 VO
     */
    @PostMapping
    public ApiResponse<ConversationVO> add(@Valid @RequestBody(required = false) ConversationAddDTO dto) {
        if (dto == null) {
            dto = new ConversationAddDTO();
        }
        return ApiResponse.ok(conversationService.addConversation(dto));
    }

    /**
     * 会话列表,对应接口第 3.5 节。
     *
     * @param query 查询条件 支持 projectId、archived、limit、cursor,可空
     * @return 会话分页 VO
     */
    @PostMapping("/list")
    public ApiResponse<ConversationPageVO> list(@Valid @RequestBody(required = false) ConversationQueryDTO query) {
        if (query == null) {
            query = new ConversationQueryDTO();
        }
        return ApiResponse.ok(conversationService.pageConversations(query));
    }

    /**
     * 重命名会话,对应接口第 3.8 节。
     *
     * @param conversationId 会话 id 路径参数
     * @param dto            重命名请求体 title 必填且 1~255 字符
     * @return 重命名后的会话 VO
     */
    @PostMapping("/{conversationId}/rename")
    public ApiResponse<ConversationVO> rename(@PathVariable Long conversationId,
                                              @Valid @RequestBody ConversationRenameDTO dto) {
        return ApiResponse.ok(conversationService.renameConversation(conversationId, dto));
    }

    /**
     * 删除(归档)会话,对应接口第 3.2 节。软删除,可恢复。
     *
     * @param conversationId 会话 id 路径参数
     * @return 空响应
     */
    @PostMapping("/{conversationId}/archive")
    public ApiResponse<Void> archive(@PathVariable Long conversationId) {
        conversationService.archiveConversation(conversationId);
        return ApiResponse.ok();
    }

    /**
     * 恢复会话,对应接口第 3.3 节。
     *
     * @param conversationId 会话 id 路径参数
     * @return 恢复后的会话 VO
     */
    @PostMapping("/{conversationId}/restore")
    public ApiResponse<ConversationVO> restore(@PathVariable Long conversationId) {
        return ApiResponse.ok(conversationService.restoreConversation(conversationId));
    }

    /**
     * 彻底删除会话,对应接口第 3.4 节。不可恢复。
     *
     * @param conversationId 会话 id 路径参数
     * @return 空响应
     */
    @PostMapping("/{conversationId}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long conversationId) {
        conversationService.permanentlyDeleteConversation(conversationId);
        return ApiResponse.ok();
    }

    /**
     * 移动会话到指定项目,对应接口第 3.6 节。projectId 为 null 表示移到「无项目」分组。
     *
     * @param conversationId 会话 id 路径参数
     * @param dto            移动请求体 projectId 可为 null
     * @return 移动后的会话 VO
     */
    @PostMapping("/{conversationId}/project")
    public ApiResponse<ConversationVO> move(@PathVariable Long conversationId,
                                            @Valid @RequestBody(required = false) ConversationMoveDTO dto) {
        if (dto == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "请求体不能为空");
        }
        return ApiResponse.ok(conversationService.moveConversation(conversationId, dto));
    }

    /**
     * 发送消息并流式接收回复 SSE,对应 API 文档第 5.1 节。
     *
     * 响应头 Content-Type text/event-stream。事件类型见 service 层注释。
     *
     * @param conversationId 会话 id 路径参数
     * @param dto            消息内容 content 1~4000 字符
     * @return SseEmitter 由 Spring 异步写入
     */
    @PostMapping(value = "/{conversationId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessage(@PathVariable Long conversationId,
                                  @Valid @RequestBody MessageSendDTO dto) {
        log.info("[sse] sendMessage conv={} len={}", conversationId,
                dto == null || dto.getContent() == null ? 0 : dto.getContent().length());
        return conversationService.sendMessageStream(conversationId, dto);
    }

    /**
     * 停止当前生成,对应 API 文档第 5.2 节简化方案。
     *
     * 本期实现直接返回当前会话中最后一条 assistant 消息 id。
     * 实际停止由前端断流触发,后端在断流时不再推新事件。
     * 真 Dify 时改为取消底层 HTTP 请求。
     *
     * @param conversationId 会话 id 路径参数
     * @param userId        可选 Dify task id 本期忽略
     * @return 简化的停止响应
     */
    @PostMapping("/{conversationId}/messages/stop")
    public ApiResponse<java.util.Map<String, Object>> stop(@PathVariable Long conversationId,
                                                           @org.springframework.web.bind.annotation.RequestHeader(value = "X-User-Id", required = false) String userId) {
        log.info("[sse] stop conv={} userId={}", conversationId, userId);
        return ApiResponse.ok(java.util.Map.of("incomplete", true));
    }
}
