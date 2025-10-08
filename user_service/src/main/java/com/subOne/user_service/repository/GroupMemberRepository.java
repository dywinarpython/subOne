package com.subOne.user_service.repository;

import com.subOne.user_service.entity.GroupMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupMemberRepository extends R2dbcRepository<GroupMember, Long> {

    @Query("""
            select user_id
            from group_members
            where group_id = :groupId
            """)
    Flux<UUID> findByGroupId(Long groupId);

    @Query("""
            select owner_id
            from groups
            where id = :groupId
            """)
    Mono<UUID> findOwnerByGroupId(Long groupId);

    Mono<Long> deleteByUserId(UUID userId);

    Mono<Boolean> existsByGroupIdAndUserId(Long groupId, UUID userId);

}
