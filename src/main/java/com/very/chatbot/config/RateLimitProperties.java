package com.very.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 限流配置。
 *
 * 对应 application.yml 的 chatbot.* 段。
 *
 * @author chatbot
 */
@Data
@Component
@ConfigurationProperties(prefix = "chatbot")
public class RateLimitProperties {

    /**
     * 单用户每分钟最大请求数(超出后返回 1004 / 429)。
     */
    private int rateLimitPerMinute = 30;

    /**
     * 会话标题截取长度。首条 user 消息,API 文档第 3.1 节默认为"新对话"。
     */
    private int titleLength = 20;

    /**
     * SSE 消息 content 最大长度,超出返回错误码 1001 与 HTTP 400。
     */
    private int maxContentLength = 4000;
}
