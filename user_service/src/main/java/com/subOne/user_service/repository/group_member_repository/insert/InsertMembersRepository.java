package com.subOne.user_service.repository.group_member_repository.insert;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@FunctionalInterface
public interface InsertMembersRepository {
    Mono<Void> insertAllMembers(List<Integer> groupsId, UUID userId);
}
