package com.very.chatbot.dify;

/**
 * Dify 客户端抽象。
 *
 * Mock 与真实现都实现本接口,供上层按 dify.mock 切换。
 * 所有方法在调用方的当前线程或异步线程中执行,实现需要保证线程安全。
 *
 * @author chatbot
 */
public interface DifyClient {

    /**
     * 发起一次流式对话请求。
     *
     * 实现必须:发起 HTTP POST 到 {baseUrl}/chat-messages 或模拟它;
     * 在收到片段时回调 DifyStreamListener.onDelta;
     * 在收到 message_end 时回调 DifyStreamListener.onMessageEnd;
     * 在异常时回调 DifyStreamListener.onError。
     *
     * @param query               用户原文
     * @param difyConversationId  上下文中的 Dify 会话 id;首轮为 null
     * @param user                Dify 端 user 标识,本期用 X-User-Id
     * @param listener            流式回调,单次对话内顺序回调
     */
    void chatMessageStream(String query, String difyConversationId, String user, DifyStreamListener listener);
}
