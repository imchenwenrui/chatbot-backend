package com.very.chatbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.very.chatbot.entity.ConversationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会话表 Mapper。
 *
 * @author chatbot
 */
@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
}
