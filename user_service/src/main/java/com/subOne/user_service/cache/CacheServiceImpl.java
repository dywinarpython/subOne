package com.subOne.user_service.cache;

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

    private final ObjectMapper objectMapper;

    @Override
    public <T> Mono<T> getValue(Object key, Class<T> clazz) {
        return redisTemplate.opsForValue().get(key.toString())
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
        return redisTemplate.opsForValue().set(key, value, duration)
                .onErrorResume(ex -> Mono.empty())
                .flatMap(bl -> {
                    if(!bl) {
                        log.warn("Cache is not save: key: {}, value: {}", key, value);
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> deleteValue(String key) {
        return redisTemplate.delete(key)
                .onErrorResume(ex -> Mono.empty())
                .flatMap(count -> {
                    if(count == 0) {
                        log.warn("Cache is not delete: key: {}", key);
                    }
                    return Mono.empty();
                });
    }


}
