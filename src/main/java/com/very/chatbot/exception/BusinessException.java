package com.very.chatbot.exception;

import com.very.chatbot.enums.ApiCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * 项目内约定:业务层不抛 {@link RuntimeException},统一抛本异常,由
 * {@link GlobalExceptionHandler} 统一转换为 ApiResponse。
 *
 * @author chatbot
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务码(对应 ApiCode) */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ApiCode.INTERNAL_ERROR.getCode();
    }

    public BusinessException(ApiCode apiCode) {
        super(apiCode.getMessage());
        this.code = apiCode.getCode();
    }

    public BusinessException(ApiCode apiCode, String message) {
        super(message);
        this.code = apiCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
