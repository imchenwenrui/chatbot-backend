package com.very.chatbot.exception;

import com.very.chatbot.common.ApiResponse;
import com.very.chatbot.enums.ApiCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理。
 *
 * 将各类异常统一转换为 ApiResponse,确保所有 controller 返回结构一致。
 *
 * @author chatbot
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 失败响应
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        log.warn("[biz] code={} message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理 @Valid 校验失败(@RequestBody)。
     *
     * @param e 校验异常
     * @return 失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? ApiCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
        log.warn("[valid] {}", msg);
        return ApiResponse.fail(ApiCode.BAD_REQUEST, msg);
    }

    /**
     * 处理 form binding 校验失败(@ModelAttribute)。
     *
     * @param e 绑定异常
     * @return 失败响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? ApiCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
        log.warn("[bind] {}", msg);
        return ApiResponse.fail(ApiCode.BAD_REQUEST, msg);
    }

    /**
     * 处理缺少必填 query/form 参数。
     *
     * @param e 缺少参数异常
     * @return 失败响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingParam(MissingServletRequestParameterException e) {
        String msg = "缺少必填参数:" + e.getParameterName();
        log.warn("[missing-param] {}", msg);
        return ApiResponse.fail(ApiCode.BAD_REQUEST, msg);
    }

    /**
     * 处理请求体解析失败(JSON 格式错误等)。
     *
     * @param e 请求体不可读异常
     * @return 失败响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("[not-readable] {}", e.getMessage());
        return ApiResponse.fail(ApiCode.BAD_REQUEST, "请求体格式错误");
    }

    /**
     * 处理 HTTP 方法不支持。
     *
     * @param e 方法不支持异常
     * @return 失败响应
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[method] {}", e.getMessage());
        return ApiResponse.fail(ApiCode.BAD_REQUEST.getCode(), "HTTP 方法不被支持");
    }

    /**
     * 兜底异常。
     *
     * @param e 任意异常
     * @return 失败响应
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleAll(Throwable e) {
        log.error("[error] unhandled", e);
        return ApiResponse.fail(ApiCode.INTERNAL_ERROR);
    }
}
