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
    Flux<UUID> findMembersIdByGroupId(Long groupId);

    @Query("""
            select count(*) < 5
            from group_members
            where group_id = :groupId
            """)
    Mono<Boolean> existsMembersInGroupIsNoMoreFive(Long groupId);




    @Query("""
           SELECT EXISTS (
                SELECT 1
                FROM groups g
                WHERE g.id = :groupId
                     AND (
                      g.owner_id = :userId
                      OR EXISTS (
                          SELECT 1
                          FROM group_members gm
                          WHERE gm.user_id = :userId AND  gm.group_id = :groupId)
                      )
           )
    """)
    Mono<Boolean> existsByUserIdAndGroupId(UUID userId, Long groupId);

    Mono<Long> deleteByUserId(UUID userId);

    Mono<Boolean> existsByGroupIdAndUserId(Long groupId, UUID userId);

}
