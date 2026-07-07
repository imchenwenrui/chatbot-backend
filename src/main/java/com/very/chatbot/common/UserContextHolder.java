package com.very.chatbot.common;

/**
 * 当前请求用户上下文,基于 ThreadLocal。
 *
 * 由 {@code UserContextInterceptor} 在请求开始时设置,
 * 在 afterCompletion 时清理。
 *
 * 本期不做登录鉴权,默认用户为 {@link #DEFAULT_USER}。后续接 JWT 时
 * 在拦截器里换成真实用户 id 即可,service 层调用点不变。
 *
 * @author chatbot
 */
public final class UserContextHolder {

    /** 匿名占位用户(本期不接登录) */
    public static final String DEFAULT_USER = "anonymous";

    private static final ThreadLocal<String> USER = new ThreadLocal<>();

    private UserContextHolder() {
    }

    /**
     * 设置当前用户。
     *
     * @param userId 用户标识,null 视为 {@link #DEFAULT_USER}
     */
    public static void set(String userId) {
        USER.set(userId == null || userId.isEmpty() ? DEFAULT_USER : userId);
    }

    /**
     * 获取当前用户;未设置返回 {@link #DEFAULT_USER}。
     *
     * @return 用户标识
     */
    public static String get() {
        String u = USER.get();
        return u == null ? DEFAULT_USER : u;
    }

    /**
     * 清理当前线程用户(请求结束时调用,避免线程复用导致串号)。
     */
    public static void clear() {
        USER.remove();
    }
}
