package com.subOne.user_service.repository;

import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.Group;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupRepository extends R2dbcRepository<Group, Long> {

    Mono<Integer> deleteByIdAndOwnerId(Long groupId, UUID ownerId);

    Mono<Boolean> existsByIdAndOwnerId(Long groupId, UUID ownerId);

    Flux<ResponseGroupDto> findByOwnerId(UUID ownerId);

    @Query("""
            select g.id, g.name, g.created_at, g.updated_at
            from groups g
            join group_members gm on gm.group_id = g.id
            where gm.user_id = :userId
            """)
    Flux<ResponseGroupDto> findGroupsUserIsMember(UUID userId);

    @Query(
            """
            select g.id , name,  created_at, updated_at
            from groups g
            where g.id = :groupId and (g.owner_id = :userId or
                exists(
                    select 1
                    from "group_members" gm
                    where gm.user_id = :userId
            ))
            """
    )
    Mono<ResponseGroupDto> findGroupById(Long groupId, UUID userId);

    @Query("""
            select owner_id, u.name, u.surname, u.email
            from groups g
            join users u on u.user_id = g.owner_id
            where g.id = :groupId
            """)
    Mono<ResponseUserDto> findOwnerByGroupId(Long groupId);

    @Modifying
    @Query(
            """
            update groups
            set name = :name,
                updated_at = NOW()
            where id = :groupId and owner_id = :ownerId
            """
    )
    Mono<Integer> updateGroupByIdAndOwnerId(Long groupId, String name, UUID ownerId);



}
