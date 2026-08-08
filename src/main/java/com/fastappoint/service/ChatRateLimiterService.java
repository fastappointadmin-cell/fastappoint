package com.fastappoint.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, per-IP fixed-window rate limiter for the chat WebSocket endpoint.
 * Limits how many LLM requests a single IP can make within a rolling time window,
 * preventing runaway token spend on OpenAI (or any other LLM provider).
 *
 * Configurable via:
 *   app.chat.rate-limit.max-requests  (default 20)
 *   app.chat.rate-limit.window-seconds (default 60)
 */
@Service
public class ChatRateLimiterService {

    private final int maxRequests;
    private final long windowMs;

    private final ConcurrentHashMap<String, long[]> windows = new ConcurrentHashMap<>();

    public ChatRateLimiterService(
            @Value("${app.chat.rate-limit.max-requests:20}") int maxRequests,
            @Value("${app.chat.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000L;
    }

    /**
     * Returns true if the request is allowed, false if the IP has exceeded the limit.
     * Thread-safe: uses ConcurrentHashMap.compute() for atomic window updates.
     *
     * Window state: long[0] = window start epoch ms, long[1] = request count in window.
     */
    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        long[] result = windows.compute(ip, (key, current) -> {
            if (current == null || now - current[0] >= windowMs) {
                return new long[]{now, 1};
            }
            current[1]++;
            return current;
        });
        return result[1] <= maxRequests;
    }
}
