package com.very.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.very.chatbot.config.RateLimitProperties;
import com.very.chatbot.convert.ConversationConvert;
import com.very.chatbot.dto.ConversationAddDTO;
import com.very.chatbot.dto.ConversationMoveDTO;
import com.very.chatbot.dto.ConversationQueryDTO;
import com.very.chatbot.dto.ConversationRenameDTO;
import com.very.chatbot.dto.MessageSendDTO;
import com.very.chatbot.dify.DifyClient;
import com.very.chatbot.dify.DifyClientFactory;
import com.very.chatbot.dify.DifyStreamListener;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    private final DifyClientFactory difyClientFactory; // dify / mock 客户端
    private final RateLimitProperties bizProperties; // title 截取 / content 长度上限

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

    // ================================================================
    //  SSE 流式发送消息 对应 API 文档第 5.1 节
    // ================================================================

    /**
     * 发送消息并流式推送给前端,基于 SseEmitter。
     *
     * 流程:校验会话存在与未归档(已有 requireExists,需补 archived 校验);
     * 写 user 消息;创建 assistant 占位消息;注册 emitter 回调,包括完成、超时与错误;
     * 调 Dify 流,DifyStreamListener 把片段推给 emitter,累加到 assistant 缓冲,
     * message_end 时落库;首轮拿到的 difyConversationId 写回 t_conversation.dify_conv_id;
     * 首条 user 消息触发 title 异步更新。
     *
     * 注:流式过程发生在 Dify 或 Mock 自己的线程,本方法只需组装 emitter 后立即返回。
     */
    @Override
    public SseEmitter sendMessageStream(Long conversationId, MessageSendDTO dto) {
        // content 长度由 @Size 在 Controller 入口校验,这里只防御一次
        if (dto == null || dto.getContent() == null || dto.getContent().isEmpty()) {
            throw new BusinessException(ApiCode.BAD_REQUEST, "消息内容不能为空");
        }
        if (dto.getContent().length() > bizProperties.getMaxContentLength()) {
            throw new BusinessException(ApiCode.BAD_REQUEST,
                    "消息内容超过 " + bizProperties.getMaxContentLength() + " 字符上限");
        }

        ConversationEntity conv = requireExists(conversationId);
        if (conv.getArchived() != null && conv.getArchived() == 1) {
            throw new BusinessException(ApiCode.CONVERSATION_DELETED, "会话已归档,无法发送消息");
        }

        // 1) 写 user 消息(不在事务里,独立提交;失败就整体失败)
        MessageEntity userMsg = new MessageEntity();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(dto.getContent());
        userMsg.setIncomplete(0);
        messageMapper.insert(userMsg);

        // 2) assistant 占位(先有 id,content 后续累加,message_end 时落库)
        MessageEntity assistantMsg = new MessageEntity();
        assistantMsg.setConversationId(conversationId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent("");
        assistantMsg.setIncomplete(0);
        messageMapper.insert(assistantMsg);

        // 3) emitter
        SseEmitter emitter = new SseEmitter(0L); // 0 = 不过期
        String assistantId = String.valueOf(assistantMsg.getId());
        StringBuilder buf = new StringBuilder();
        AtomicReference<String> difyConvIdRef = new AtomicReference<>(conv.getDifyConvId());
        AtomicReference<String> difyMsgIdRef = new AtomicReference<>("");

        // 客户端断线:取消 Dify 请求(由 Mock 内部用 flag 处理,真 Dify 这里 cancel HTTP)
        emitter.onCompletion(() -> log.info("[sse] complete conv={} msg={}", conversationId, assistantId));
        emitter.onTimeout(() -> log.warn("[sse] timeout conv={} msg={}", conversationId, assistantId));
        emitter.onError(t -> log.warn("[sse] error conv={} msg={} err={}", conversationId, assistantId, t.getMessage()));

        // 4) 推 first-frame:让前端立刻知道 assistantId(便于 UI 关联)
        try {
            emitter.send(SseEmitter.event()
                    .name("ready")
                    .data(java.util.Map.of(
                            "type", "ready",
                            "userMessageId", String.valueOf(userMsg.getId()),
                            "assistantMessageId", assistantId
                    )));
        } catch (IOException e) {
            log.warn("[sse] ready send failed: {}", e.getMessage());
        }

        // 5) 调 Dify / Mock 拉流(异步线程,不阻塞 Controller)
        DifyClient client = difyClientFactory.client();
        DifyStreamListener listener = new DifyStreamListener() {
            @Override
            public void onDelta(String delta) {
                if (delta == null) return;
                buf.append(delta);
                try {
                    emitter.send(SseEmitter.event()
                            .name("message")
                            .data(java.util.Map.of("type", "delta", "delta", delta)));
                } catch (IOException e) {
                    log.warn("[sse] delta send failed: {}", e.getMessage());
                }
            }

            @Override
            public void onMessageEnd(String difyConversationId, String difyMessageId, String usage) {
                if (difyMessageId != null) {
                    difyMsgIdRef.set(difyMessageId);
                }
                // 落库 assistant 消息(content + difyMessageId)
                MessageEntity upd = new MessageEntity();
                upd.setId(assistantMsg.getId());
                upd.setContent(buf.toString());
                upd.setIncomplete(0);
                if (difyMessageId != null && !difyMessageId.isEmpty()) {
                    upd.setDifyMessageId(difyMessageId);
                }
                messageMapper.updateById(upd);

                // 首轮拿到 difyConversationId → 回写
                if (difyConversationId != null && !difyConversationId.isEmpty()
                        && (difyConvIdRef.get() == null || difyConvIdRef.get().isEmpty())) {
                    ConversationEntity updConv = new ConversationEntity();
                    updConv.setId(conversationId);
                    updConv.setDifyConvId(difyConversationId);
                    conversationMapper.updateById(updConv);
                    difyConvIdRef.set(difyConversationId);
                }

                // 推 done
                try {
                    emitter.send(SseEmitter.event()
                            .name("done")
                            .data(java.util.Map.of(
                                    "type", "done",
                                    "messageId", assistantId,
                                    "usage", usage == null ? "{}" : usage
                            )));
                } catch (IOException e) {
                    log.warn("[sse] done send failed: {}", e.getMessage());
                }
                emitter.complete();
            }

            @Override
            public void onError(int code, String message) {
                // 落库:标 incomplete=1,content 保留已收到的部分
                MessageEntity upd = new MessageEntity();
                upd.setId(assistantMsg.getId());
                upd.setContent(buf.toString());
                upd.setIncomplete(1);
                messageMapper.updateById(upd);

                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(java.util.Map.of(
                                    "type", "error",
                                    "code", code,
                                    "message", message == null ? "推流失败" : message
                            )));
                } catch (IOException e) {
                    log.warn("[sse] error send failed: {}", e.getMessage());
                }
                emitter.complete();
            }

            @Override
            public void onStopped(String difyMessageId) {
                // 当前 Mock 不区分 stop / done,真 Dify 时会用到
                log.info("[sse] stopped conv={} msg={}", conversationId, assistantId);
            }
        };

        // 6) 异步启动拉流(避免阻塞 Controller)
        String userId = com.very.chatbot.common.UserContextHolder.get();
        new Thread(() -> {
            try {
                client.chatMessageStream(dto.getContent(), difyConvIdRef.get(), userId, listener);
            } catch (Exception e) {
                log.error("[sse] stream thread error", e);
                listener.onError(ApiCode.DIFY_INVOKE_FAILED.getCode(), "推流出错:" + e.getMessage());
            }
        }, "sse-conv-" + conversationId).start();

        // 7) 首条 user 消息后异步改 title
        long existing = countMessages(conversationId);
        if (existing <= 2L) { // user + assistant 占位 = 2 条
            changeTitleAsync(conversationId, dto.getContent());
        }

        return emitter;
    }

    /**
     * 异步更新会话标题(取首条 user 消息前 N 字符)。
     */
    @Async
    public void changeTitleAsync(Long conversationId, String firstUserContent) {
        try {
            int len = bizProperties.getTitleLength();
            String title = firstUserContent;
            if (title == null) return;
            title = title.replaceAll("\\s+", " ").trim();
            if (title.length() > len) {
                title = title.substring(0, len);
            }
            if (title.isEmpty()) return;

            ConversationEntity upd = new ConversationEntity();
            upd.setId(conversationId);
            upd.setTitle(title);
            conversationMapper.updateById(upd);
            log.info("[sse] title updated conv={} title={}", conversationId, title);
        } catch (Exception e) {
            log.warn("[sse] changeTitleAsync failed: {}", e.getMessage());
        }
    }
}
