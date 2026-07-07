package com.very.chatbot.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 项目列表查询条件 DTO 对应接口 POST /api/projects/list。
 *
 * @author chatbot
 */
@Data
public class ProjectQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 单页大小 默认 50 最大 100 范围 1~100 */
    @Min(value = 1, message = "limit 不能小于 1")
    @Max(value = 100, message = "limit 不能大于 100")
    private Integer limit;

    /** 上一页最后一条的 updatedAt 用于游标分页 */
    private String cursor;
}
