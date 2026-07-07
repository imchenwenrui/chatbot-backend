package com.very.chatbot.dify;

import com.very.chatbot.enums.ApiCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dify Mock 客户端。
 *
 * dify.mock=true 时启用,不发真 HTTP,按时间间隔推假 delta 帧。
 * 用于本地与 CI 环境跑通前后端链路,接入真 Dify 时翻 dify.mock=false。
 *
 * 行为约定:首轮生成 mock-conv-{uuid} 作为 difyConversationId,通过 message_end 回调返回;
 * 推送时把 query 包成多行 markdown 模拟回复,按 DifyProperties.getMockIntervalMs 推 1 到 3 字符的 delta;
 * 消息 id 生成 mock-msg-{uuid};
 * 统计 usage 简单填写 prompt 与 completion tokens;
 * 30 秒看门狗,超时则强制结束本轮,避免永久挂起。
 *
 * @author chatbot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DifyMockClient implements DifyClient {

    private final DifyProperties properties;

    /** 单线程调度即可,Mock 不需要并行;且调度任务都是短的 */
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2, r -> {
                Thread t = new Thread(r, "dify-mock-stream");
                t.setDaemon(true);
                return t;
            });

    @Override
    public void chatMessageStream(String query, String difyConversationId, String user,
                                  DifyStreamListener listener) {
        final String convId = (difyConversationId == null || difyConversationId.isEmpty())
                ? "mock-conv-" + UUID.randomUUID() : difyConversationId;
        final String msgId = "mock-msg-" + UUID.randomUUID();

        String reply = buildMockReply(query);
        final int total = reply.length();

        // 拆成 ~60 段流式推,体现"逐字输出"体验
        final int chunkSize = Math.max(1, total / 60);
        long interval = Math.max(20L, properties.getMockIntervalMs());
        final AtomicInteger cursor = new AtomicInteger(0);
        final AtomicBoolean stopped = new AtomicBoolean(false);

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            if (stopped.get()) {
                return;
            }
            try {
                int pushed = cursor.get();
                if (pushed >= total) {
                    finishOk(listener, convId, msgId);
                    stopped.set(true);
                    return;
                }
                int end = Math.min(total, pushed + chunkSize);
                String delta = reply.substring(pushed, end);
                cursor.set(end);
                listener.onDelta(delta);
                if (end >= total) {
                    finishOk(listener, convId, msgId);
                    stopped.set(true);
                }
            } catch (Exception e) {
                log.error("[dify-mock] stream error", e);
                listener.onError(ApiCode.DIFY_INVOKE_FAILED.getCode(),
                        "Mock 推流出错:" + e.getMessage());
                stopped.set(true);
            }
        }, 50, interval, TimeUnit.MILLISECONDS);

        // 30s 看门狗
        scheduler.schedule(() -> {
            if (stopped.compareAndSet(false, true)) {
                log.warn("[dify-mock] watchdog timeout, force finish convId={}", convId);
                try {
                    listener.onError(ApiCode.DIFY_TIMEOUT.getCode(), "Mock 超时兜底结束");
                } catch (Exception ignore) {
                }
            }
            task.cancel(false);
        }, 30, TimeUnit.SECONDS);
    }

    private void finishOk(DifyStreamListener listener, String convId, String msgId) {
        String usage = "{\"prompt_tokens\":12,\"completion_tokens\":42,\"total_tokens\":54}";
        try {
            listener.onMessageEnd(convId, msgId, usage);
        } catch (Exception e) {
            log.warn("[dify-mock] onMessageEnd listener throw: {}", e.getMessage());
        }
    }

    /**
     * 构造一段体现 Markdown 渲染能力的假回复。
     */
    private static String buildMockReply(String query) {
        return "已收到你的问题:**" + safePreview(query, 30) + "**\n\n"
                + "我目前是 **Dify Mock 模式**(后端默认开启,yml 中 `dify.mock=true`)。\n"
                + "接入真 Dify 时:把 `dify.mock` 翻为 false,并配置 `base-url` / `api-key` / `chatflow-id`。\n\n"
                + "下面是一些示例内容,你可以看到代码块、列表、引用都被正确渲染:\n\n"
                + "```java\n"
                + "@GetMapping(\"/hello\")\n"
                + "public String hello() {\n"
                + "    return \"world\";\n"
                + "}\n"
                + "```\n\n"
                + "- 列表项 1\n"
                + "- 列表项 2\n"
                + "- 列表项 3\n\n"
                + "> 这是一段引用,用于展示 markdown 样式。\n\n"
                + "你也可以试试在左侧栏「新建对话」「重命名」「归档」「恢复」,或者把会话移到不同项目。";
    }

    private static String safePreview(String s, int max) {
        if (s == null) return "";
        String t = s.replaceAll("\\s+", " ").trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }
}
