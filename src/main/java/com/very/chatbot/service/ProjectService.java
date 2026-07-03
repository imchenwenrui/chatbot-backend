package com.very.chatbot.service;

import com.very.chatbot.dto.ProjectAddDTO;
import com.very.chatbot.dto.ProjectUpdateDTO;
import com.very.chatbot.entity.ProjectEntity;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.vo.ProjectPageVO;
import com.very.chatbot.vo.ProjectVO;

/**
 * 项目 Service。
 *
 * 对应 docs/API文档.md 第 4 章。
 *
 * @author chatbot
 */
public interface ProjectService {

    /**
     * 新建项目。
     *
     * @param dto 新建请求,name 必填 1~50 字符,description 可选 0~200 字符
     * @return 新建后的项目 VO(conversationCount=0)
     * @throws BusinessException 名称重复时抛 3003
     */
    ProjectVO addProject(ProjectAddDTO dto);

    /**
     * 修改项目(重命名 / 改描述)。
     *
     * @param projectId 项目 id
     * @param dto       修改请求,name / description 至少传一个
     * @return 修改后的项目 VO
     * @throws BusinessException 项目不存在抛 3001;名称冲突抛 3003
     */
    ProjectVO updateProject(Long projectId, ProjectUpdateDTO dto);

    /**
     * 查询项目列表(本期不分页直接返回,游标参数预留)。
     *
     * @param limit  单页大小,默认 50,最大 100
     * @param cursor 上一页最后一条的 updatedAt,可空(首版忽略)
     * @return 项目分页 VO(nextCursor 始终为 null)
     */
    ProjectPageVO pageProject(Integer limit, String cursor);

    /**
     * 查询单个项目。
     *
     * @param projectId 项目 id
     * @return 项目 VO(含 conversationCount)
     * @throws BusinessException 项目不存在抛 3001
     */
    ProjectVO getProject(Long projectId);

    /**
     * 删除项目(项目下必须无未归档会话)。
     *
     * @param projectId 项目 id
     * @throws BusinessException 项目不存在抛 3001;项目下仍有未归档会话抛 3002
     */
    void deleteProject(Long projectId);

    /**
     * 校验项目是否存在(给 Service 之间调用)。
     *
     * @param projectId 项目 id
     * @return 项目实体
     * @throws BusinessException 找不到时抛 3001
     */
    ProjectEntity requireExists(Long projectId);
}
