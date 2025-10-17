package com.subOne.subscriptions_service.repository.user_subscription_repository.update;

import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.util.Map;
@FunctionalInterface
public interface UpdateRepository {
    <T> Mono<Long> updateFieldsByField(Map<SqlIdentifier, Object> sqlIdentifierObjectMap, Class<T> classT, String column, Object columnValue);
}
