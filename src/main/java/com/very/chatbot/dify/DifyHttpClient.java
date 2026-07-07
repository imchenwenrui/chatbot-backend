package com.very.chatbot.dify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.very.chatbot.enums.ApiCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 真 Dify 客户端,流式。
 *
 * dify.mock=false 时由 DifyClientFactory 选用。
 * 行为约定见 API 文档第 6.2 与 6.3 节。
 *
 * 协议要点:POST {base-url}/chat-messages,response_mode=streaming;
 * SSE 帧形如 event: message 后接 data: {"event":"message","answer":"..."};
 * 首轮 conversation_id 为空,后续复用 Dify 返回的同一个值;
 * 按 event 类型分发,涵盖 message、message_end、error、ping、agent_message,后者过滤。
 *
 * 本实现是真实联调时使用,本期默认不启用,Mock 模式为主。保留为后续工作。
 *
 * @author chatbot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyHttpClient implements DifyClient {

    private final DifyProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void chatMessageStream(String query, String difyConversationId, String user,
                                  DifyStreamListener listener) {
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            listener.onError(ApiCode.DIFY_INVOKE_FAILED.getCode(), "Dify api-key 未配置");
            return;
        }
        try {
            String body = objectMapper.writeValueAsString(java.util.Map.of(
                    "inputs", java.util.Map.of(),
                    "query", query,
                    "response_mode", "streaming",
                    "conversation_id", difyConversationId == null ? "" : difyConversationId,
                    "user", user == null ? "anonymous" : user,
                    "files", java.util.List.of()
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl() + "/chat-messages"))
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofLines())
                    .body()
                    .forEach(line -> parseLine(line, listener));

        } catch (Exception e) {
            log.error("[dify-http] invoke failed", e);
            listener.onError(ApiCode.DIFY_INVOKE_FAILED.getCode(), "调用 Dify 失败:" + e.getMessage());
        }
    }

    /**
     * 解析单行:仅识别 {@code event: ...} 与 {@code data: ...} 两类行。
     * 其它(空行、注释、心跳)忽略。
     */
    private void parseLine(String line, DifyStreamListener listener) {
        if (line == null || line.isEmpty()) return;
        if (line.startsWith(":")) return; // 注释
        if (line.startsWith("data:")) {
            String data = line.substring(5).trim();
            try {
                JsonNode node = objectMapper.readTree(data);
                String event = node.path("event").asText("");
                switch (event) {
                    case "message":
                        listener.onDelta(node.path("answer").asText(""));
                        break;
                    case "message_end":
                        String convId = node.path("conversation_id").asText("");
                        String msgId = node.path("id").asText("");
                        String usage = node.path("metadata").path("usage").toString();
                        listener.onMessageEnd(convId, msgId, usage);
                        break;
                    case "error":
                        listener.onError(ApiCode.DIFY_BAD_RESPONSE.getCode(),
                                node.path("message").asText("Dify 返回错误"));
                        break;
                    case "agent_message":
                    case "ping":
                    default:
                        // 过滤:agent_message 是工作流节点中间产物,ping 是心跳
                        break;
                }
            } catch (Exception e) {
                log.warn("[dify-http] parse data line failed: {}", e.getMessage());
                listener.onError(ApiCode.DIFY_BAD_RESPONSE.getCode(), "Dify 返回非法响应");
            }
        }
        // event: 单独行不处理(我们的简化方案从 data.event 字段读事件)
    }
}
