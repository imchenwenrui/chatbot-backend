package com.very.chatbot.service;

import com.very.chatbot.entity.MessageEntity;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.vo.MessagePageVO;

/**
 * 消息 Service。
 *
 * 对应 API 文档第 3.7 节历史消息与第 3.9 节反馈。
 *
 * @author chatbot
 */
public interface MessageService {

    /**
     * 查询某会话下的消息历史(按创建时间正序)。
     *
     * @param conversationId 会话 id
     * @param limit          单页大小,默认 50,最大 200
     * @return 消息分页 VO(无 nextCursor,简化为单页)
     * @throws BusinessException 会话不存在抛 1002
     */
    MessagePageVO pageMessagesByConversation(Long conversationId, Integer limit);

    /**
     * 对某条消息设置反馈 / 清除反馈。
     *
     * @param conversationId 会话 id(校验归属用)
     * @param messageId      消息 id
     * @param feedback       like / dislike / null
     * @throws BusinessException 会话不存在抛 1002;消息不存在或不属于该会话抛 4001
     */
    void setFeedback(Long conversationId, Long messageId, String feedback);

    /**
     * 校验并获取消息(给 Service 之间调用),同时校验消息属于指定会话。
     *
     * @param conversationId 会话 id(校验归属用)
     * @param messageId      消息 id
     * @return 消息实体
     * @throws BusinessException 会话不存在抛 1002;消息不存在或不属于该会话抛 4001
     */
    MessageEntity requireExists(Long conversationId, Long messageId);
}
