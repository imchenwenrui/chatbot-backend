package com.very.chatbot.convert;

import com.very.chatbot.entity.ProjectEntity;
import com.very.chatbot.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 项目 Entity <-> VO 转换器。
 *
 * @author chatbot
 */
@Component
public class ProjectConvert {

    /**
     * Entity 转 VO(含基类字段)。
     *
     * @param entity 项目实体
     * @return 项目 VO
     */
    public ProjectVO toVO(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCreatedAt(entity.getCreateTime());
        vo.setUpdatedAt(entity.getUpdateTime());
        return vo;
    }
}
