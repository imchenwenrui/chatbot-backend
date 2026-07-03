package com.very.chatbot.convert;

import com.very.chatbot.entity.MessageEntity;
import com.very.chatbot.vo.MessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 消息 Entity <-> VO 转换器。
 *
 * @author chatbot
 */
@Component
public class MessageConvert {

    /**
     * Entity 转 VO(含基类字段)。
     *
     * @param entity 消息实体
     * @return 消息 VO
     */
    public MessageVO toVO(MessageEntity entity) {
        if (entity == null) {
            return null;
        }
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setCreatedAt(entity.getCreateTime());
        vo.setIncomplete(entity.getIncomplete() != null && entity.getIncomplete() == 1);
        return vo;
    }
}
