package com.very.chatbot.convert;

import com.very.chatbot.entity.ConversationEntity;
import com.very.chatbot.vo.ConversationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 会话 Entity <-> VO 转换器。
 *
 * @author chatbot
 */
@Component
public class ConversationConvert {

    /**
     * Entity 转 VO(含基类字段)。
     *
     * @param entity 会话实体
     * @return 会话 VO
     */
    public ConversationVO toVO(ConversationEntity entity) {
        if (entity == null) {
            return null;
        }
        ConversationVO vo = new ConversationVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCreatedAt(entity.getCreateTime());
        vo.setUpdatedAt(entity.getUpdateTime());
        vo.setArchived(entity.getArchived() != null && entity.getArchived() == 1);
        return vo;
    }
}
