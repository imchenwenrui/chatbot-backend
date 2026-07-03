package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 修改项目请求 DTO(PATCH /api/projects/{id})。
 * name / description 至少传一个。
 *
 * @author chatbot
 */
@Data
public class ProjectUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目名称,1~50 字符(可选) */
    @Size(min = 1, max = 50, message = "项目名称长度 1~50 字符")
    private String name;

    /** 项目描述,0~200 字符(可选) */
    @Size(max = 200, message = "项目描述长度 0~200 字符")
    private String description;

    /**
     * 至少传一个字段(name 或 description)。
     *
     * @return true 表示校验通过
     */
    @AssertTrue(message = "name 和 description 至少传一个")
    public boolean isAnyPresent() {
        return (name != null && !name.isEmpty()) || (description != null);
    }
}
