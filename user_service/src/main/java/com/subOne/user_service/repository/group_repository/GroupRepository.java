package com.subOne.user_service.repository.group_repository;

import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.entity.Group;
import com.subOne.user_service.repository.group_repository.select.SelectOwnerGroupRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends R2dbcRepository<Group, Long>, SelectOwnerGroupRepository {

    Mono<Integer> deleteByIdAndOwnerId(Long groupId, UUID ownerId);

    Mono<Boolean> existsByIdAndOwnerId(Long groupId, UUID ownerId);

    Flux<ResponseGroupDto> findByOwnerId(UUID ownerId, Pageable pageable);

    @Query("""
            select owner_id
            from groups
            where id = :groupId
            """)
    Mono<UUID> findOwnerIdByGroupId(Long groupId);

    @Query("""
            select id
            from groups
            where owner_id = :ownerId
            """)
    Flux<Long> findGroupsIdByOwnerId(UUID ownerId);

    @Query("""
            select count(*)
            from groups g
            where g.owner_id = :ownerId and g.id in (:groupsId)
            """)
    Mono<Long> findCountWhereUserIsOwnerByGroupsId(UUID ownerId, List<Long> groupsId);

    @Query("""
            select count(*)
            from groups g
            where owner_id = :ownerId
            """)
    Mono<Integer> findCountByOwnerId(UUID ownerId);

    @Query("""
            select g.id, g.name, g.created_at, g.updated_at
            from groups g
            join group_members gm on gm.group_id = g.id
            where gm.user_id = :userId
            offset :page
            limit :pageSize
            """)
    Flux<ResponseGroupDto> findGroupsUserIsMember(UUID userId, Integer page, Integer pageSize);

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
