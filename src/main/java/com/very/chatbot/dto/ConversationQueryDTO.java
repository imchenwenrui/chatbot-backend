package com.very.chatbot.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 会话列表查询条件 DTO 对应接口 POST /api/conversations/list。
 *
 * @author chatbot
 */
@Data
public class ConversationQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 限定到某项目下的会话;null 表示全部 */
    private Long projectId;

    /** 是否查询归档列表;默认 false */
    private Boolean archived;

    /** 单页大小,默认 50,最大 100 */
    private Integer limit;

    /** 上一页最后一条的 updatedAt,用于游标分页 */
    private String cursor;
}
