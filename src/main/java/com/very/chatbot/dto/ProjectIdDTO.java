package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 项目 id 请求 DTO
 * 用于 POST /api/projects/{projectId}/* 这类仅靠路径参数无法表达全部入参,或在请求体里再次携带项目 id 的接口。
 *
 * @author chatbot
 */
@Data
public class ProjectIdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目 id,必填 */
    @NotNull(message = "项目 id 不能为空")
    private Long projectId;
}
