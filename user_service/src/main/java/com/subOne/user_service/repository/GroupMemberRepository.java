package com.subOne.user_service.repository;

import com.subOne.user_service.dto.group_member.GroupMemberInfoDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.GroupMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupMemberRepository extends R2dbcRepository<GroupMember, Long> {

    Mono<Long> deleteByUserId(UUID userId);

    Mono<Boolean> existsByGroupIdAndUserId(Long groupId, UUID userId);


    @Query("""
            select u.user_id, u.name, u.surname, u.email
            from group_members g
            join users u on u.user_id = g.user_id
            where group_id = :groupId
            """)
    Flux<ResponseUserDto> findMembersIdByGroupId(Long groupId);

    @Query("""
    SELECT
        EXISTS (
            SELECT 1
            FROM groups g
            LEFT JOIN group_members gm ON g.id = gm.group_id AND gm.user_id = :userId
            WHERE g.id = :groupId AND (g.owner_id = :userId OR gm.user_id IS NOT NULL)
        ) as exist,
        CAST((SELECT COUNT(*) FROM group_members WHERE group_id = :groupId) AS BIGINT) AS count
    """)
    Mono<GroupMemberInfoDto> findExistUserInGroupAndCountMemberInGroup(UUID userId, Long groupId);
}
