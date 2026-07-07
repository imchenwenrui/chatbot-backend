package com.very.chatbot.dify;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Dify 客户端工厂。
 *
 * 按 dify.mock 选择真或假实现。注入 DifyClient 时,
 * 仍建议注入此工厂后取 client(),避免每次手动判断。
 *
 * @author chatbot
 */
@Component
@RequiredArgsConstructor
public class DifyClientFactory {

    private final DifyProperties properties;
    private final DifyMockClient mockClient;
    private final DifyHttpClient httpClient;

    /**
     * 取当前应当使用的客户端(单例选择,结果在 yml 固定后不会变)。
     */
    public DifyClient client() {
        return properties.isMock() ? mockClient : httpClient;
    }

    /**
     * 是否处于 Mock 模式(给上层做"是否需要真 conversation_id 回写"等判断用)。
     */
    public boolean isMock() {
        return properties.isMock();
    }
}
