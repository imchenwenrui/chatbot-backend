package com.very.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.very.chatbot.convert.ConversationConvert;
import com.very.chatbot.dto.ConversationAddDTO;
import com.very.chatbot.dto.ConversationMoveDTO;
import com.very.chatbot.dto.ConversationQueryDTO;
import com.very.chatbot.dto.ConversationRenameDTO;
import com.very.chatbot.entity.ConversationEntity;
import com.very.chatbot.entity.MessageEntity;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.mapper.ConversationMapper;
import com.very.chatbot.mapper.MessageMapper;
import com.very.chatbot.service.ConversationService;
import com.very.chatbot.service.ProjectService;
import com.very.chatbot.vo.ConversationPageVO;
import com.very.chatbot.vo.ConversationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话 Service 实现。
 *
 * @author chatbot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final ConversationConvert conversationConvert;
    private final ProjectService projectService; // 复用项目存在性校验

    /**
     * 新建会话。
     *
     * @param dto 新建请求
     * @return 新建后的会话 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO addConversation(ConversationAddDTO dto) {
        Long projectId = dto.getProjectId();
        if (projectId != null && projectId > 0) {
            projectService.requireExists(projectId);
        }

        ConversationEntity entity = new ConversationEntity();
        entity.setProjectId(projectId == null ? 0L : projectId);
        entity.setTitle("新对话");
        entity.setUserId(""); // 本期匿名占位
        entity.setDifyConvId("");
        entity.setArchived(0);

        conversationMapper.insert(entity);
        log.info("[conversation] add id={} projectId={}", entity.getId(), entity.getProjectId());

        ConversationVO vo = conversationConvert.toVO(entity);
        vo.setMessageCount(0L);
        return vo;
    }

    /**
     * 会话列表查询。
     *
     * @param query 查询条件
     * @return 会话分页 VO
     */
    @Override
    public ConversationPageVO pageConversations(ConversationQueryDTO query) {
        Integer limit = query.getLimit();
        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 100);
        boolean archived = Boolean.TRUE.equals(query.getArchived());

        LambdaQueryWrapper<ConversationEntity> wrapper = new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getDeleted, 0)
                .eq(ConversationEntity::getArchived, archived ? 1 : 0)
                .orderByDesc(ConversationEntity::getUpdateTime)
                .last("LIMIT " + size);

        if (query.getProjectId() != null) {
            // 0 表示限定到「无项目」分组
            wrapper.eq(ConversationEntity::getProjectId, query.getProjectId());
        }

        List<ConversationEntity> entities = conversationMapper.selectList(wrapper);

        ConversationPageVO page = new ConversationPageVO();
        List<ConversationVO> items = new ArrayList<>(entities.size());
        for (ConversationEntity e : entities) {
            ConversationVO vo = conversationConvert.toVO(e);
            vo.setMessageCount(countMessages(e.getId()));
            items.add(vo);
        }
        page.setItems(items);
        page.setNextCursor(null);
        return page;
    }

    /**
     * 重命名会话。
     *
     * @param conversationId 会话 id
     * @param dto            重命名请求
     * @return 重命名后的会话 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO renameConversation(Long conversationId, ConversationRenameDTO dto) {
        ConversationEntity entity = requireExists(conversationId);
        entity.setTitle(dto.getTitle());
        conversationMapper.updateById(entity);
        log.info("[conversation] rename id={} title={}", entity.getId(), entity.getTitle());
        ConversationVO vo = conversationConvert.toVO(entity);
        vo.setMessageCount(countMessages(entity.getId()));
        return vo;
    }

    /**
     * 归档会话。
     *
     * @param conversationId 会话 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void archiveConversation(Long conversationId) {
        ConversationEntity entity = requireExists(conversationId);
        if (entity.getArchived() != null && entity.getArchived() == 1) {
            return; // 已归档,幂等返回
        }
        entity.setArchived(1);
        conversationMapper.updateById(entity);
        log.info("[conversation] archive id={}", entity.getId());
    }

    /**
     * 恢复会话。
     *
     * @param conversationId 会话 id
     * @return 恢复后的会话 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO restoreConversation(Long conversationId) {
        ConversationEntity entity = requireExists(conversationId);
        if (entity.getArchived() == null || entity.getArchived() == 0) {
            throw new BusinessException(ApiCode.CONVERSATION_DELETED,
                    "会话未被归档,无须恢复");
        }
        entity.setArchived(0);
        conversationMapper.updateById(entity);
        log.info("[conversation] restore id={}", entity.getId());
        ConversationVO vo = conversationConvert.toVO(entity);
        vo.setMessageCount(countMessages(entity.getId()));
        return vo;
    }

    /**
     * 彻底删除会话(物理删除其下消息与会话本身)。
     *
     * @param conversationId 会话 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentlyDeleteConversation(Long conversationId) {
        ConversationEntity entity = requireExists(conversationId);
        // 物理删除其下消息,然后删除会话本身
        messageMapper.delete(new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, entity.getId()));
        conversationMapper.deleteById(entity.getId());
        log.info("[conversation] permanent-delete id={}", entity.getId());
    }

    /**
     * 移动会话到指定项目。
     *
     * @param conversationId 会话 id
     * @param dto            移动请求
     * @return 移动后的会话 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO moveConversation(Long conversationId, ConversationMoveDTO dto) {
        ConversationEntity entity = requireExists(conversationId);
        Long target = dto.getProjectId();
        if (target != null && target > 0) {
            projectService.requireExists(target);
        }
        entity.setProjectId(target == null ? 0L : target);
        conversationMapper.updateById(entity);
        log.info("[conversation] move id={} -> projectId={}", entity.getId(), entity.getProjectId());
        ConversationVO vo = conversationConvert.toVO(entity);
        vo.setMessageCount(countMessages(entity.getId()));
        return vo;
    }

    /**
     * 查询单个会话。
     *
     * @param conversationId 会话 id
     * @return 会话 VO
     */
    @Override
    public ConversationVO getConversation(Long conversationId) {
        ConversationEntity entity = requireExists(conversationId);
        ConversationVO vo = conversationConvert.toVO(entity);
        vo.setMessageCount(countMessages(entity.getId()));
        return vo;
    }

    /**
     * 校验会话存在。
     *
     * @param conversationId 会话 id
     * @return 会话实体
     */
    @Override
    public ConversationEntity requireExists(Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            throw new BusinessException(ApiCode.CONVERSATION_NOT_FOUND);
        }
        ConversationEntity entity = conversationMapper.selectById(conversationId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ApiCode.CONVERSATION_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 统计某会话下的有效消息数(逻辑删除 = 0)。
     *
     * @param conversationId 会话 id
     * @return 消息条数
     */
    private long countMessages(Long conversationId) {
        Long n = messageMapper.selectCount(new LambdaQueryWrapper<MessageEntity>()
                .eq(MessageEntity::getConversationId, conversationId)
                .eq(MessageEntity::getDeleted, 0));
        return n == null ? 0L : n;
    }
}
