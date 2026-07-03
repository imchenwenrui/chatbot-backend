package com.very.chatbot.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息分页 VO。
 *
 * @author chatbot
 */
@Data
public class MessagePageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<MessageVO> items;
}
