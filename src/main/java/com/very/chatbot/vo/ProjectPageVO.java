package com.very.chatbot.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 项目分页 VO。
 *
 * @author chatbot
 */
@Data
public class ProjectPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ProjectVO> items;
    private String nextCursor;
}
