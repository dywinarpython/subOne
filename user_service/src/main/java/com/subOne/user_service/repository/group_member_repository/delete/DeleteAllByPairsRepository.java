package com.subOne.user_service.repository.group_member_repository.delete;

import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.UUID;

public interface DeleteAllByPairsRepository {
    Mono<Void> deleteAllByUserGroupPairs(@Param("userGroupPairs") List<Tuple2<UUID, Long>> pairs);
}
