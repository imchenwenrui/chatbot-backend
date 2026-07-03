package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.ConversationAddDTO;
import com.very.chatbot.dto.ConversationMoveDTO;
import com.very.chatbot.dto.ConversationQueryDTO;
import com.very.chatbot.dto.ConversationRenameDTO;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.service.ConversationService;
import com.very.chatbot.vo.ConversationPageVO;
import com.very.chatbot.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 会话接口(对应 docs/API文档.md 第 3 章,不含消息相关)。
 *
 * 流式发送消息(SSE)和停止生成属于 5.1 / 5.2,留到 dify 集成阶段补完。
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
     * 新建会话(对应 3.1)。
     *
     * @param dto 新建请求体,可空;projectId 为 null 表示「无项目」分组
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
     * 会话列表(对应 3.5)。
     *
     * @param query 查询条件(支持 projectId / archived / limit / cursor)
     * @return 会话分页 VO
     */
    @GetMapping
    public ApiResponse<ConversationPageVO> list(@ModelAttribute ConversationQueryDTO query) {
        return ApiResponse.ok(conversationService.pageConversations(query));
    }

    /**
     * 重命名会话(对应 3.8)。
     *
     * @param conversationId 会话 id(路径参数)
     * @param dto            重命名请求体,title 必填且 1~255 字符
     * @return 重命名后的会话 VO
     */
    @PatchMapping("/{conversationId}")
    public ApiResponse<ConversationVO> rename(@PathVariable Long conversationId,
                                              @Valid @RequestBody ConversationRenameDTO dto) {
        return ApiResponse.ok(conversationService.renameConversation(conversationId, dto));
    }

    /**
     * 删除(归档)会话(对应 3.2),软删除,可恢复。
     *
     * @param conversationId 会话 id(路径参数)
     * @return 空响应
     */
    @DeleteMapping("/{conversationId}")
    public ApiResponse<Void> archive(@PathVariable Long conversationId) {
        conversationService.archiveConversation(conversationId);
        return ApiResponse.ok();
    }

    /**
     * 恢复会话(对应 3.3)。
     *
     * @param conversationId 会话 id(路径参数)
     * @return 恢复后的会话 VO
     */
    @PostMapping("/{conversationId}/restore")
    public ApiResponse<ConversationVO> restore(@PathVariable Long conversationId) {
        return ApiResponse.ok(conversationService.restoreConversation(conversationId));
    }

    /**
     * 彻底删除会话(对应 3.4),不可恢复。
     *
     * @param conversationId 会话 id(路径参数)
     * @return 空响应
     */
    @DeleteMapping("/{conversationId}/permanent")
    public ApiResponse<Void> permanentDelete(@PathVariable Long conversationId) {
        conversationService.permanentlyDeleteConversation(conversationId);
        return ApiResponse.ok();
    }

    /**
     * 移动会话到指定项目(对应 3.6);projectId 为 null 表示移到「无项目」分组。
     *
     * @param conversationId 会话 id(路径参数)
     * @param dto            移动请求体,projectId 可为 null
     * @return 移动后的会话 VO
     */
    @PatchMapping("/{conversationId}/project")
    public ApiResponse<ConversationVO> move(@PathVariable Long conversationId,
                                            @Valid @RequestBody(required = false) ConversationMoveDTO dto) {
        if (dto == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "请求体不能为空");
        }
        return ApiResponse.ok(conversationService.moveConversation(conversationId, dto));
    }

    /**
     * 发送消息 — 流式 SSE,占位实现,留给 dify 集成阶段。
     *
     * 当前直接返回"暂未接入 Dify"错误,避免前端打通后无响应的尴尬。
     *
     * @param conversationId 会话 id(路径参数)
     * @param body           请求体(首版占位,后续接 dify 后实际使用)
     * @return 占位响应
     */
    @PostMapping("/{conversationId}/messages")
    public ApiResponse<Void> sendMessage(@PathVariable Long conversationId,
                                         @RequestBody(required = false) java.util.Map<String, Object> body) {
        throw new BusinessException(ApiCode.DIFY_INVOKE_FAILED, "对话 SSE 流待 dify 集成后启用");
    }
}
