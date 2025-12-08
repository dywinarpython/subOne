package com.subOne.user_service.repository.group_invite_repository;

import com.subOne.user_service.dto.group_invite.CodeDto;
import com.subOne.user_service.entity.GroupInvite;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;


public interface GroupInviteRepository extends R2dbcRepository<GroupInvite, Integer> {

    @Query("""
            select code, expires_at
            from group_invites
            where group_id = :groupId and expires_at > NOW()
            """)
    Mono<CodeDto> findCodeByGroupId(Integer groupId);

    @Query("""
            select group_id
            from group_invites
            where code = :code and expires_at > NOW()
            """)
    Mono<Integer> findGroupIdByCode(UUID code);

    Mono<Void> deleteAllByExpiresAtBefore(Instant moment);

}
