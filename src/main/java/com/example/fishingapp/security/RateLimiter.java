// src/main/java/com/example/fishingapp/security/RateLimiter.java
package com.example.fishingapp.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private static class Window {
        volatile long windowStartEpochSeconds;
        volatile int count;
    }

    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int maxAttempts, long windowSeconds) {
        long now = Instant.now().getEpochSecond();
        Window window = buckets.computeIfAbsent(key, k -> {
            Window w = new Window();
            w.windowStartEpochSeconds = now;
            w.count = 0;
            return w;
        });

        synchronized (window) {
            if (now - window.windowStartEpochSeconds >= windowSeconds) {
                window.windowStartEpochSeconds = now;
                window.count = 0;
            }
            if (window.count >= maxAttempts) {
                return false;
            }
            window.count++;
            return true;
        }
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void cleanup() {
        long now = Instant.now().getEpochSecond();
        buckets.entrySet().removeIf(entry -> {
            Window w = entry.getValue();
            synchronized (w) {
                return now - w.windowStartEpochSeconds > 3600;
            }
        });
    }
}