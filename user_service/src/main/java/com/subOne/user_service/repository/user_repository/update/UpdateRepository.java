package com.subOne.user_service.repository.user_repository.update;

import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface UpdateRepository {
    <T> Mono<Long> updateFields(Map<SqlIdentifier, Object> sqlIdentifierObjectMap, Class<T> classT, String column, Object columnValue);
}
