package com.very.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 应用启动入口。
 *
 * 启用异步支持(@Async),用于
 * ConversationServiceImpl.changeTitleAsync 异步改 title。
 *
 * @author chatbot
 */
@EnableAsync
@SpringBootApplication
public class ChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }
}
