package com.subOne.subscriptions_service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private final CacheHealthCheck cacheHealthCheck;

    private final ObjectMapper objectMapper;

    @Override
    public <T> Mono<T> getValue(Object key, Class<T> clazz) {
        if(!cacheHealthCheck.isHealth()) {
            log.debug("Cache unavailable, skipping get");
            return Mono.empty();
        }
        return redisTemplate.opsForValue().get(key.toString())
                .doOnError(ex -> {
                    log.warn("Cache read failed for key: {}. Error: {}", key, ex.getMessage());
                    cacheHealthCheck.markUnhealthy();
                })
                .onErrorResume(ex -> Mono.empty())
               .flatMap(value -> {
                    if(value == null) return Mono.empty();
                    try {
                        if (clazz.isInstance(value)){
                            return Mono.just(clazz.cast(value));
                        }
                        T converted = objectMapper.convertValue(value, clazz);
                        return Mono.just(converted);
                    } catch (Exception e) {
                        log.error("Failed to convert cache value. Key: {}, Expected type: {}, Actual type: {}",
                                key, clazz.getName(), value.getClass().getName(), e);
                        return Mono.error(new IllegalStateException(
                                "Failed to convert cache value: expected " + clazz.getName() +
                                        " but got " + value.getClass().getName(), e));
                    }
               });
    }


    @Override
    public <T> Mono<Void> saveValue(String key, T value, Duration duration) {
        if(!cacheHealthCheck.isHealth()) {
            log.debug("Cache unavailable, skipping save");
            return Mono.empty();
        }
        return redisTemplate.opsForValue().set(key, value, duration)
                .doOnError(ex -> {
                    log.warn("Cache save failed for key: {}. Error: {}", key, ex.getMessage());
                    cacheHealthCheck.markUnhealthy();
                })
                .onErrorResume(ex -> Mono.empty())
                .flatMap(bl -> {
                    if(!bl) {
                        log.error("Cache is not save: key: {}, value: {}", key, value);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> deleteValue(String key) {
        if(!cacheHealthCheck.isHealth()) {
            log.debug("Cache unavailable, skipping delete");
            return Mono.empty();
        }
        return redisTemplate.delete(key)
                .doOnError(ex -> {
                    log.warn("Cache delete failed for key: {}. Error: {}", key, ex.getMessage());
                    cacheHealthCheck.markUnhealthy();
                })
                .onErrorResume(ex -> Mono.empty())
                .flatMap(count -> {
                    if(count == 0) {
                        log.error("Cache is not delete: key: {}", key);
                    }
                    return Mono.empty();
                });
    }


}
