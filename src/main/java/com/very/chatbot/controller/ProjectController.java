package com.very.chatbot.controller;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.dto.ProjectAddDTO;
import com.very.chatbot.dto.ProjectQueryDTO;
import com.very.chatbot.dto.ProjectUpdateDTO;
import com.very.chatbot.service.ProjectService;
import com.very.chatbot.vo.ProjectPageVO;
import com.very.chatbot.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 项目接口,对应 API 文档第 4 章。
 *
 * 全部接口强制走 POST。资源 id 通过路径参数表达,业务字段通过 DTO 请求体表达。
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
     * 项目列表,对应接口第 4.1 节。
     *
     * @param query 查询条件 支持 limit 与 cursor 可空
     * @return 项目分页 VO
     */
    @PostMapping("/list")
    public ApiResponse<ProjectPageVO> list(@Valid @RequestBody(required = false) ProjectQueryDTO query) {
        Integer limit = query == null ? null : query.getLimit();
        String cursor = query == null ? null : query.getCursor();
        return ApiResponse.ok(projectService.pageProject(limit, cursor));
    }

    /**
     * 新建项目,对应接口第 4.2 节。
     *
     * @param dto 新建请求体 name 必填且 1~50 字符 description 可选 0~200 字符
     * @return 新建后的项目 VO
     */
    @PostMapping
    public ApiResponse<ProjectVO> add(@Valid @RequestBody ProjectAddDTO dto) {
        return ApiResponse.ok(projectService.addProject(dto));
    }

    /**
     * 重命名或改描述项目,对应接口第 4.3 节。
     *
     * @param projectId 项目 id 路径参数
     * @param dto       修改请求体 name 与 description 至少传一个
     * @return 修改后的项目 VO
     */
    @PostMapping("/{projectId}/update")
    public ApiResponse<ProjectVO> update(@PathVariable Long projectId,
                                         @Valid @RequestBody ProjectUpdateDTO dto) {
        return ApiResponse.ok(projectService.updateProject(projectId, dto));
    }

    /**
     * 删除项目,对应接口第 4.4 节。项目下必须无未归档会话,否则返回错误码 3002。
     *
     * @param projectId 项目 id 路径参数
     * @return 空响应
     */
    @PostMapping("/{projectId}/delete")
    public ApiResponse<Void> delete(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ApiResponse.ok();
    }
}
