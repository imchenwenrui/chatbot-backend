package com.very.chatbot.dify;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Dify 集成配置,对应 application.yml 的 dify.* 段。
 *
 * 本类只描述如何与 Dify 对接的配置;接入真 Dify 时
 * 需要配置 baseUrl、apiKey、chatflowId 并把 mock 翻为 false。
 *
 * @author chatbot
 */
@Data
@Component
@ConfigurationProperties(prefix = "dify")
public class DifyProperties {

    /** mock=true 时使用本地假流式,不发起真 HTTP;默认 true */
    private boolean mock = true;

    /** Dify API 根地址,例 https://api.dify.ai/v1 */
    private String baseUrl = "https://api.dify.ai/v1";

    /** Dify 应用 API Key(以 app- 开头) */
    private String apiKey = "";

    /**
     * Dify 应用 id(或 chatflow id)。Dify 调用时通过路径
     * {@code /chat-messages} 并不直接使用 id,但保留配置便于未来切到 workflow / completion。
     */
    private String chatflowId = "";

    /** 真 Dify HTTP 调用超时(毫秒) */
    private long timeoutMs = 60_000L;

    /** mock 模式下每段 delta 推送间隔(毫秒) */
    private long mockIntervalMs = 80L;
}
