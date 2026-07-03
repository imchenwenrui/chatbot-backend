package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.ProjectAddDTO;
import com.very.chatbot.dto.ProjectUpdateDTO;
import com.very.chatbot.service.ProjectService;
import com.very.chatbot.vo.ProjectPageVO;
import com.very.chatbot.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 项目接口(对应 docs/API文档.md 第 4 章)。
 *
 * @author chatbot
 */
@Slf4j
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 项目列表(对应 4.1)。
     *
     * @param limit  单页大小,默认 50,最大 100
     * @param cursor 上一页最后一条的 updatedAt,可空
     * @return 项目分页 VO
     */
    @GetMapping
    public ApiResponse<ProjectPageVO> list(@RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) String cursor) {
        return ApiResponse.ok(projectService.pageProject(limit, cursor));
    }

    /**
     * 新建项目(对应 4.2)。
     *
     * @param dto 新建请求体,name 必填且 1~50 字符,description 可选 0~200 字符
     * @return 新建后的项目 VO
     */
    @PostMapping
    public ApiResponse<ProjectVO> add(@Valid @RequestBody ProjectAddDTO dto) {
        return ApiResponse.ok(projectService.addProject(dto));
    }

    /**
     * 重命名 / 改描述项目(对应 4.3)。
     *
     * @param projectId 项目 id(路径参数)
     * @param dto       修改请求体,name / description 至少传一个
     * @return 修改后的项目 VO
     */
    @PatchMapping("/{projectId}")
    public ApiResponse<ProjectVO> update(@PathVariable Long projectId,
                                         @Valid @RequestBody ProjectUpdateDTO dto) {
        return ApiResponse.ok(projectService.updateProject(projectId, dto));
    }

    /**
     * 删除项目(对应 4.4,项目下必须无未归档会话,否则返回 3002)。
     *
     * @param projectId 项目 id(路径参数)
     * @return 空响应
     */
    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> delete(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ApiResponse.ok();
    }
}
