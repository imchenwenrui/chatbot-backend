package com.very.chatbot.common;

import com.very.chatbot.enums.ApiCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装。
 *
 * 与 docs/API文档.md 第 1.4 节「通用响应结构」一致,JSON 形如:
 *
 *   "code": 0
 *   "message": "ok"
 *   "data": ...
 *
 * @param <T> 业务数据类型
 * @author chatbot
 */
@Data
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务码 */
    private Integer code;

    /** 描述 */
    private String message;

    /** 业务数据 */
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应(无 data)。
     *
     * @param <T> 业务数据类型
     * @return 成功包装
     */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应(带 data)。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功包装
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ApiCode.SUCCESS.getCode(), ApiCode.SUCCESS.getMessage(), data);
    }

    /**
     * 业务失败响应。
     *
     * @param apiCode 业务码枚举
     * @return 失败包装
     */
    public static <T> ApiResponse<T> fail(ApiCode apiCode) {
        return new ApiResponse<>(apiCode.getCode(), apiCode.getMessage(), null);
    }

    /**
     * 业务失败响应(自定义描述)。
     *
     * @param apiCode 业务码枚举
     * @param message 自定义描述
     * @return 失败包装
     */
    public static <T> ApiResponse<T> fail(ApiCode apiCode, String message) {
        return new ApiResponse<>(apiCode.getCode(), message, null);
    }

    /**
     * 通过 code / message 直接构造失败(供异常处理使用)。
     *
     * @param code    业务码
     * @param message 描述
     * @return 失败包装
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
