package com.subOne.user_service.service_impl;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.GroupMemberRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {

    private final MapperGroupMember mapperGroupMember;

    private final GroupService groupService;

    private final CacheService cacheService;

    private final GroupMemberRepository groupMemberRepository;


    @Override
    // TODO уменьшить количество запросов
    public Mono<ResponseMembersDto> addUser(UUID userId, Long groupId) {
        return groupMemberRepository.existsByUserIdAndGroupId(userId, groupId)
                .flatMap( bl -> {
                    if (bl) return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of the group"));
                    return Mono.empty();
                })
                .then(Mono.defer( () -> groupMemberRepository.existsMembersInGroupIsNoMoreFive(groupId)))
                .flatMap(bl -> {
                    if (!bl) return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "There can be no more than 5 members of the group (the owner is not considered)"));
                    return Mono.empty();
                })
                .then(Mono.defer(() -> groupMemberRepository.save(mapperGroupMember.userIdAndGroupIdToGroupMember(userId, groupId))))
                .then(Mono.defer(() -> getUsersAndOwner(groupId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt) {
        String key = "MEMBER::" + jwt.getSubject() + ' ' + groupId;
        return cacheService.getValue(key, Boolean.class)
                .flatMap(hasAccess -> {
                    if (hasAccess) return Mono.just(true);
                    return Mono.empty();
                })
                .switchIfEmpty(
                        checkUserInGroup(groupId, jwt)
                )
                .then(Mono.defer(() -> getUsersAndOwner(groupId)));
    }


    @Override
    @Transactional
    @CacheEvict(value = "MEMBER", key = "#userId + ' ' + #groupId")
    public Mono<Void> deleteMember(Long groupId, UUID userId, Jwt jwt) {
        return Mono.just(jwt.getSubject())
                .map(UUID::fromString)
                .flatMap(jwtUserId -> {
                    if (jwtUserId.equals(userId)) {
                        return Mono.error(new AccessDeniedException("The user cannot delete himself"));
                    }
                    return groupService.checkUserIsOwner(groupId, jwt)
                            .then(groupMemberRepository.deleteByUserId(userId)
                                    .flatMap(count -> {
                                        if (count == 0)
                                            return Mono.error(new NoSuchElementException("User is not found"));
                                        return Mono.empty();
                                    }));
                });
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "MEMBER", key = "#jwt.getSubject() + ' ' + #groupId")
    public Mono<Boolean> checkUserInGroup(Long groupId, Jwt jwt){
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, UUID.fromString(jwt.getSubject())).flatMap(
                exists -> {
                    if(!exists) return groupService.checkUserIsOwner(groupId, jwt);
                    String key = "MEMBER::" + jwt.getSubject() + ' ' + groupId;
                    return cacheService.saveValue(key, true, Duration.ofMinutes(15)).thenReturn(true);
                }
        );
    }

    private Mono<ResponseMembersDto> getUsersAndOwner(Long groupId){
        return groupService.getOwnerId(groupId).flatMap( ownerId -> {
            Flux<ResponseMemberDto> members = groupMemberRepository.findMembersIdByGroupId(groupId).map(id -> new ResponseMemberDto(id, false));
            return Flux.concat(
                    Flux.just(new ResponseMemberDto(ownerId, true)),
                    members
            ).collectList().map(ResponseMembersDto::new);
        });
    }

}
