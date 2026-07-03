package com.very.chatbot.service;

import com.very.chatbot.dto.ConversationAddDTO;
import com.very.chatbot.dto.ConversationMoveDTO;
import com.very.chatbot.dto.ConversationQueryDTO;
import com.very.chatbot.dto.ConversationRenameDTO;
import com.very.chatbot.entity.ConversationEntity;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.vo.ConversationPageVO;
import com.very.chatbot.vo.ConversationVO;

/**
 * 会话 Service。
 *
 * 对应 docs/API文档.md 第 3 章(不含流式发送消息与反馈)。
 *
 * @author chatbot
 */
public interface ConversationService {

    /**
     * 新建会话。
     *
     * @param dto 新建请求(可指定 projectId;不传表示「无项目」分组)
     * @return 新建后的会话 VO
     * @throws BusinessException 指定了 projectId 但项目不存在时抛 3001
     */
    ConversationVO addConversation(ConversationAddDTO dto);

    /**
     * 会话列表查询(支持 projectId / archived 过滤)。
     *
     * @param query 查询条件
     * @return 会话分页 VO(nextCursor 简化为 null)
     */
    ConversationPageVO pageConversations(ConversationQueryDTO query);

    /**
     * 重命名会话。
     *
     * @param conversationId 会话 id
     * @param dto            重命名请求,title 必填 1~255 字符
     * @return 重命名后的会话 VO
     * @throws BusinessException 会话不存在抛 1002;已逻辑删除抛 1003
     */
    ConversationVO renameConversation(Long conversationId, ConversationRenameDTO dto);

    /**
     * 归档会话(软删除,前端默认列表隐藏但仍可恢复)。
     *
     * @param conversationId 会话 id
     * @throws BusinessException 会话不存在抛 1002
     */
    void archiveConversation(Long conversationId);

    /**
     * 恢复已归档会话。
     *
     * @param conversationId 会话 id
     * @return 恢复后的会话 VO
     * @throws BusinessException 会话不存在抛 1002;未被归档抛 1003
     */
    ConversationVO restoreConversation(Long conversationId);

    /**
     * 彻底删除会话(物理删除会话及其下消息)。
     *
     * @param conversationId 会话 id
     * @throws BusinessException 会话不存在抛 1002
     */
    void permanentlyDeleteConversation(Long conversationId);

    /**
     * 移动会话到指定项目(projectId 为 null 表示无项目)。
     *
     * @param conversationId 会话 id
     * @param dto            移动请求,projectId 可为 null
     * @return 移动后的会话 VO
     * @throws BusinessException 会话不存在抛 1002;目标项目不存在抛 3001
     */
    ConversationVO moveConversation(Long conversationId, ConversationMoveDTO dto);

    /**
     * 查询单个会话。
     *
     * @param conversationId 会话 id
     * @return 会话 VO(含 messageCount)
     * @throws BusinessException 会话不存在抛 1002
     */
    ConversationVO getConversation(Long conversationId);

    /**
     * 校验会话存在(给 Service 之间调用)。
     *
     * @param conversationId 会话 id
     * @return 会话实体
     * @throws BusinessException 找不到时抛 1002
     */
    ConversationEntity requireExists(Long conversationId);
}
