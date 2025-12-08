package com.subOne.subscriptions_service.cache;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface CacheService {
    <T> Mono<T> getValue(Object key, Class<T> clazz);
    <T> Mono<Void> saveValue(String key, T value, Duration duration);
    Mono<Void> deleteValue(String key);
}
