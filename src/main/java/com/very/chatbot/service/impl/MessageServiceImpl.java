package com.very.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.very.chatbot.convert.MessageConvert;
import com.very.chatbot.entity.MessageEntity;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.mapper.MessageMapper;
import com.very.chatbot.service.ConversationService;
import com.very.chatbot.service.MessageService;
import com.very.chatbot.vo.MessagePageVO;
import com.very.chatbot.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息 Service 实现。
 *
 * @author chatbot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final MessageConvert messageConvert;
    private final ConversationService conversationService; // 复用会话存在性校验

    /**
     * 查询某会话下的消息历史(按创建时间正序)。
     *
     * @param conversationId 会话 id
     * @param limit          单页大小
     * @return 消息分页 VO
     */
    @Override
    public MessagePageVO pageMessagesByConversation(Long conversationId, Integer limit) {
        // 先确认会话存在
        conversationService.requireExists(conversationId);

        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 200);

        List<MessageEntity> entities = messageMapper.selectList(new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, conversationId)
                .eq(MessageEntity::getDeleted, 0)
                .orderByAsc(MessageEntity::getCreateTime)
                .last("LIMIT " + size));

        MessagePageVO page = new MessagePageVO();
        List<MessageVO> items = new ArrayList<>(entities.size());
        for (MessageEntity e : entities) {
            items.add(messageConvert.toVO(e));
        }
        page.setItems(items);
        return page;
    }

    /**
     * 设置 / 清除消息反馈。
     *
     * @param conversationId 会话 id
     * @param messageId      消息 id
     * @param feedback       like / dislike / null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setFeedback(Long conversationId, Long messageId, String feedback) {
        MessageEntity entity = requireExists(conversationId, messageId);
        // 允许 null 表示清除反馈
        entity.setFeedback(feedback);
        messageMapper.updateById(entity);
        log.info("[message] feedback id={} feedback={}", entity.getId(), feedback);
    }

    /**
     * 校验消息存在且属于指定会话。
     *
     * @param conversationId 会话 id
     * @param messageId      消息 id
     * @return 消息实体
     */
    @Override
    public MessageEntity requireExists(Long conversationId, Long messageId) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException(ApiCode.MESSAGE_NOT_FOUND);
        }
        // 顺带校验会话归属,确保不会跨会话访问
        conversationService.requireExists(conversationId);

        MessageEntity entity = messageMapper.selectById(messageId);
        if (entity == null
                || entity.getDeleted() == 1
                || !entity.getConversationId().equals(conversationId)) {
            throw new BusinessException(ApiCode.MESSAGE_NOT_FOUND);
        }
        return entity;
    }
}
