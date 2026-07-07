package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 新建项目请求 DTO 对应接口 POST /api/projects。
 *
 * @author chatbot
 */
@Data
public class ProjectAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 项目名称,1~50 字符 */
    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 50, message = "项目名称长度 1~50 字符")
    private String name;

    /** 项目描述,0~200 字符 */
    @Size(max = 200, message = "项目描述长度 0~200 字符")
    private String description;
}
