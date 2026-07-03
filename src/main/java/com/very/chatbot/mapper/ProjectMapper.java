package com.very.chatbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.very.chatbot.entity.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目表 Mapper。
 *
 * @author chatbot
 */
@Mapper
public interface ProjectMapper extends BaseMapper<ProjectEntity> {
}
