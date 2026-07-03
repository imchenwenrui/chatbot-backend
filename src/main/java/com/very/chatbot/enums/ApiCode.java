package com.very.chatbot.enums;

import lombok.Getter;

/**
 * 业务码枚举。
 *
 * 与 docs/API文档.md v0.2 中第 1.5 节「通用错误码」一一对应。
 *
 * @author chatbot
 */
@Getter
public enum ApiCode {

    /** 成功 */
    SUCCESS(0, 200, "ok"),

    /** 请求参数缺失或格式错误 */
    BAD_REQUEST(1001, 400, "请求参数缺失或格式错误"),

    /** 会话不存在 */
    CONVERSATION_NOT_FOUND(1002, 404, "会话不存在"),

    /** 会话已删除 */
    CONVERSATION_DELETED(1003, 409, "会话已删除"),

    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(1004, 429, "请求过于频繁"),

    /** 调用 Dify 失败 */
    DIFY_INVOKE_FAILED(2001, 500, "调用 Dify 失败"),

    /** Dify 返回非法响应 */
    DIFY_BAD_RESPONSE(2002, 502, "Dify 返回非法响应"),

    /** Dify 响应超时 */
    DIFY_TIMEOUT(2003, 504, "Dify 响应超时"),

    /** 项目不存在 */
    PROJECT_NOT_FOUND(3001, 404, "项目不存在"),

    /** 项目下仍有会话 */
    PROJECT_NOT_EMPTY(3002, 409, "项目下仍有会话,删除前请先移动或删除"),

    /** 项目名称重复 */
    PROJECT_NAME_DUPLICATED(3003, 409, "项目名称重复"),

    /** 消息不存在 */
    MESSAGE_NOT_FOUND(4001, 404, "消息不存在"),

    /** 服务内部异常 */
    INTERNAL_ERROR(9999, 500, "服务内部异常");

    /**
     * 业务码(与 docs/API文档.md 一致)
     */
    private final int code;

    /**
     * 建议的 HTTP 状态码,仅供 GlobalExceptionHandler 参考
     */
    private final int httpStatus;

    /**
     * 默认错误描述
     */
    private final String message;

    ApiCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 根据 code 查找枚举;找不到返回 INTERNAL_ERROR。
     *
     * @param code 业务码
     * @return 对应的 ApiCode
     */
    public static ApiCode of(int code) {
        for (ApiCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return INTERNAL_ERROR;
    }
}
