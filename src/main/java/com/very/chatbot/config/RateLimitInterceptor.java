package com.very.chatbot.config;

import com.very.chatbot.common.UserContextHolder;
import com.very.chatbot.enums.ApiCode;
import com.very.chatbot.exception.BusinessException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流拦截器,基于令牌桶实现(Bucket4j)。
 *
 * 策略:单用户每分钟 N 次,默认 30,可由 yml chatbot.rate-limit-per-minute 调整。
 * 用户维度按 X-User-Id 隔离,空值按 UserContextHolder.DEFAULT_USER 归并。
 *
 * 对应 API 文档第 1.5 节错误码 1004 与 HTTP 429,以及第 7 节安全与限制说明。
 *
 * @author chatbot
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitProperties properties;

    /** 每用户一个 Bucket,key = userId */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = UserContextHolder.get();
        Bucket bucket = buckets.computeIfAbsent(userId, this::newBucket);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            return true;
        }
        long waitSeconds = Math.max(1L, probe.getNanosToWaitForRefill() / 1_000_000_000L);
        log.warn("[rate-limit] userId={} path={} wait={}s", userId, request.getRequestURI(), waitSeconds);
        throw new BusinessException(ApiCode.TOO_MANY_REQUESTS,
                "请求过于频繁,请 " + waitSeconds + " 秒后重试");
    }

    /**
     * 给指定用户构造一个 N/分钟的令牌桶。
     */
    private Bucket newBucket(String userId) {
        int perMinute = properties.getRateLimitPerMinute();
        Bandwidth limit = Bandwidth.classic(perMinute,
                Refill.intervally(perMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
