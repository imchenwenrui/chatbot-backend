package com.very.chatbot.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话响应 VO。
 *
 * @author chatbot
 */
@Data
public class ConversationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;

    /** 所属项目 id;null 表示无项目 */
    private Long projectId;

    /** 是否归档 */
    private Boolean archived;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 该会话下消息条数(派生) */
    private Long messageCount;
}
