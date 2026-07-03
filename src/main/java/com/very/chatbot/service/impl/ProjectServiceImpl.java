package com.very.chatbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.very.chatbot.convert.ProjectConvert;
import com.very.chatbot.dto.ProjectAddDTO;
import com.very.chatbot.dto.ProjectUpdateDTO;
import com.very.chatbot.entity.ConversationEntity;
import com.very.chatbot.entity.ProjectEntity;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.exception.BusinessException;
import com.very.chatbot.mapper.ConversationMapper;
import com.very.chatbot.mapper.ProjectMapper;
import com.very.chatbot.service.ProjectService;
import com.very.chatbot.vo.ProjectPageVO;
import com.very.chatbot.vo.ProjectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目 Service 实现。
 *
 * @author chatbot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ConversationMapper conversationMapper;
    private final ProjectConvert projectConvert;

    /**
     * 新建项目。
     *
     * @param dto 新建请求
     * @return 新建后的项目 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVO addProject(ProjectAddDTO dto) {
        // 同名校验(逻辑删除不影响)
        Long exists = projectMapper.selectCount(new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getName, dto.getName())
                .eq(ProjectEntity::getDeleted, 0));
        if (exists != null && exists > 0) {
            throw new BusinessException(ApiCode.PROJECT_NAME_DUPLICATED);
        }

        ProjectEntity entity = new ProjectEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        entity.setUserId(""); // 本期匿名占位,接入登录后改为 X-User-Id

        projectMapper.insert(entity);
        log.info("[project] add id={} name={}", entity.getId(), entity.getName());

        ProjectVO vo = projectConvert.toVO(entity);
        vo.setConversationCount(0L);
        return vo;
    }

    /**
     * 修改项目。
     *
     * @param projectId 项目 id
     * @param dto       修改请求
     * @return 修改后的项目 VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProjectVO updateProject(Long projectId, ProjectUpdateDTO dto) {
        ProjectEntity entity = requireExists(projectId);

        if (StringUtils.hasText(dto.getName())) {
            // 修改后不能与其它项目同名
            Long dup = projectMapper.selectCount(new LambdaQueryWrapper<ProjectEntity>()
                    .eq(ProjectEntity::getName, dto.getName())
                    .eq(ProjectEntity::getDeleted, 0)
                    .ne(ProjectEntity::getId, projectId));
            if (dup != null && dup > 0) {
                throw new BusinessException(ApiCode.PROJECT_NAME_DUPLICATED);
            }
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        projectMapper.updateById(entity);
        log.info("[project] update id={} name={}", entity.getId(), entity.getName());
        return enrich(projectConvert.toVO(entity));
    }

    /**
     * 项目列表查询。
     *
     * @param limit  单页大小
     * @param cursor 上一页最后一条的 updatedAt(首版忽略)
     * @return 项目分页 VO
     */
    @Override
    public ProjectPageVO pageProject(Integer limit, String cursor) {
        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 100);
        LambdaQueryWrapper<ProjectEntity> wrapper = new LambdaQueryWrapper<ProjectEntity>()
                .eq(ProjectEntity::getDeleted, 0)
                .orderByDesc(ProjectEntity::getUpdateTime)
                .last("LIMIT " + size);
        // 简化方案:首版忽略 cursor 真正分页,后续按 updatedAt < cursor 过滤
        List<ProjectEntity> entities = projectMapper.selectList(wrapper);

        ProjectPageVO page = new ProjectPageVO();
        List<ProjectVO> items = new ArrayList<>(entities.size());
        for (ProjectEntity e : entities) {
            items.add(enrich(projectConvert.toVO(e)));
        }
        page.setItems(items);
        page.setNextCursor(null);
        return page;
    }

    /**
     * 查询单个项目。
     *
     * @param projectId 项目 id
     * @return 项目 VO
     */
    @Override
    public ProjectVO getProject(Long projectId) {
        return enrich(projectConvert.toVO(requireExists(projectId)));
    }

    /**
     * 删除项目。
     *
     * @param projectId 项目 id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long projectId) {
        ProjectEntity entity = requireExists(projectId);

        // 仅检查未归档会话;已归档不阻碍删除
        Long active = conversationMapper.selectCount(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getProjectId, projectId)
                .eq(ConversationEntity::getDeleted, 0)
                .eq(ConversationEntity::getArchived, 0));
        if (active != null && active > 0) {
            throw new BusinessException(ApiCode.PROJECT_NOT_EMPTY);
        }
        projectMapper.deleteById(entity.getId());
        log.info("[project] delete id={} name={}", entity.getId(), entity.getName());
    }

    /**
     * 校验项目存在。
     *
     * @param projectId 项目 id
     * @return 项目实体
     */
    @Override
    public ProjectEntity requireExists(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ApiCode.PROJECT_NOT_FOUND);
        }
        ProjectEntity entity = projectMapper.selectById(projectId);
        if (entity == null || entity.getDeleted() == 1) {
            throw new BusinessException(ApiCode.PROJECT_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 派生项目下未归档会话数。
     *
     * @param vo 项目 VO
     * @return 填充 conversationCount 后的 VO
     */
    private ProjectVO enrich(ProjectVO vo) {
        if (vo == null) {
            return null;
        }
        Long count = conversationMapper.selectCount(new LambdaQueryWrapper<ConversationEntity>()
                .eq(ConversationEntity::getProjectId, vo.getId())
                .eq(ConversationEntity::getDeleted, 0)
                .eq(ConversationEntity::getArchived, 0));
        vo.setConversationCount(count == null ? 0L : count);
        return vo;
    }
}
