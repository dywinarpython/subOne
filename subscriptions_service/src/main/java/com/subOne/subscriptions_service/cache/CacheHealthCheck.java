package com.subOne.subscriptions_service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHealthCheck {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private final AtomicBoolean healthy = new AtomicBoolean(true);

    private static final String HEALTH_CHECK_KEY = "cache:health:ping";

    public boolean isHealth(){
        return healthy.get();
    }


    @Scheduled(fixedDelayString = "${spring.cache.health.check.interval}")
    private void checkHealth() {
        if (healthy.get()) {
            performHealthCheck();
        } else {
            attemptRecovery();
        }
    }

    private void performHealthCheck() {
        redisTemplate.opsForValue()
                .set(HEALTH_CHECK_KEY, "ping", Duration.ofSeconds(10))
                .subscribe(
                        success -> {
                            if (!success && healthy.get()) {
                                log.warn("⚠️ Redis health check returned false");
                                markUnhealthy();
                            }
                        },
                        error -> {
                            log.warn("⚠️ Redis health check failed: {}", error.getMessage());
                            markUnhealthy();
                        }
                );
    }

    private void attemptRecovery() {
        log.info("🔄 Attempting to reconnect to Redis...");
        redisTemplate.opsForValue()
                .set(HEALTH_CHECK_KEY, "recovery", Duration.ofSeconds(10))
                .subscribe(
                        value -> markHealthy(),
                        error -> log.warn("⚠️ Redis still unavailable: {}", error.getMessage(), error)
                );
    }


    public void markUnhealthy() {
        if (healthy.compareAndSet(true, false)) {
            log.error("❌ Redis cache marked as UNAVAILABLE. Switching to cache-bypass mode.");
        }
    }
    private void markHealthy() {
        if (healthy.compareAndSet(false, true)) {
            log.error("✅ Redis cache RECOVERED. Caching is now enabled.");
        }
    }


}
