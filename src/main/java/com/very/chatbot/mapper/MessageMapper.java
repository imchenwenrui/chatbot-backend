package com.very.chatbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.very.chatbot.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper。
 *
 * @author chatbot
 */
@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {
}
