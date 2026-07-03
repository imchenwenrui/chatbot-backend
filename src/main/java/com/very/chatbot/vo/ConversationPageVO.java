package com.very.chatbot.vo;

import com.very.chatbot.common.ApiResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 会话分页 VO。
 *
 * 与 {@link ApiResponse} 的 {@code data} 字段配合使用。
 *
 * @author chatbot
 */
@Data
public class ConversationPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话列表（按更新时间倒序） */
    private List<ConversationVO> items;

    /** 下一页游标，无更多数据时为 null */
    private String nextCursor;
}
