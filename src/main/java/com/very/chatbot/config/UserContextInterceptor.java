package com.very.chatbot.config;

import com.very.chatbot.common.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * X-User-Id 拦截器。
 *
 * 从请求头读取 X-User-Id,放入 UserContextHolder。
 * 本期不校验,任何值都接受;空值默认 UserContextHolder.DEFAULT_USER。
 *
 * 对应 API 文档第 1.3 节通用请求头。
 *
 * @author chatbot
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    /** 请求头字段名(与 API 文档一致) */
    public static final String HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER);
        UserContextHolder.set(userId);
        log.debug("[user] request={} userId={}", request.getRequestURI(), UserContextHolder.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
