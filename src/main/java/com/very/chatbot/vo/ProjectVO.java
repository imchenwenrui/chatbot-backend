package com.very.chatbot.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目响应 VO。
 *
 * @author chatbot
 */
@Data
public class ProjectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 id */
    private Long id;

    /** 项目名称 */
    private String name;

    /** 项目描述 */
    private String description;

    /** 项目下未归档的会话数(派生字段) */
    private Long conversationCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
