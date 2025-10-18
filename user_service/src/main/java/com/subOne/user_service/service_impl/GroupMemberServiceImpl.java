package com.subOne.user_service.service_impl;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.GroupMemberRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
@Slf4j
public class GroupMemberServiceImpl implements GroupMemberService {

    private final MapperGroupMember mapperGroupMember;

    private final GroupService groupService;

    private final CacheService cacheService;

    private final GroupMemberRepository groupMemberRepository;


    @Override
    public Mono<ResponseMembersDto> addUser(UUID userId, Long groupId) {
        return groupMemberRepository.findExistUserInGroupAndCountMemberInGroup(userId, groupId)
                .flatMap( dto -> {
                    if (dto.exist()) return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of the group"));
                    if (dto.count() >= 5) return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "There can be no more than 5 members of the group (the owner is not considered)"));
                    return Mono.empty();
                })
                .then(Mono.defer(() -> groupMemberRepository.save(mapperGroupMember.userIdAndGroupIdToGroupMember(userId, groupId))))
                .then(Mono.defer(() -> getUsersAndOwner(groupId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt) {
        return checkUserInGroup(groupId, jwt)
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
    public Mono<Boolean> checkUserInGroup(Long groupId, Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String memberKey = "MEMBER::" + userId + ' ' + groupId;
        String ownerKey = "OWNER::" + groupId;
        return cacheService.getValue(memberKey, Boolean.class)
                .switchIfEmpty(
                        cacheService.getValue(ownerKey, UUID.class)
                                .map(ownerId -> ownerId.equals(userId))
                                .switchIfEmpty(Mono.just(false))
                )
                .flatMap(isMemberOrOwner -> {
                    if (isMemberOrOwner) return Mono.just(true);
                    return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)
                            .flatMap(exists -> {
                                if (exists) {
                                    return cacheService.saveValue(memberKey, true, Duration.ofMinutes(15))
                                            .thenReturn(true);
                                }
                                return groupService.checkUserIsOwnerWithoutCacheGet(groupId, jwt);
                            });
                });
    }

    @Override
    public Mono<Boolean> checkUserIsOwnerGroup(Long groupId, Jwt jwt) {
        return groupService.checkUserIsOwner(groupId, jwt);
    }


    private Mono<ResponseMembersDto> getUsersAndOwner(Long groupId){
        return groupService.getOwner(groupId).flatMap( ownerId -> {
            Flux<ResponseMemberDto> members = groupMemberRepository.findMembersIdByGroupId(groupId).map(id -> new ResponseMemberDto(id, false));
            return Flux.concat(
                    Flux.just(new ResponseMemberDto(ownerId, true)),
                    members
            ).collectList().map(ResponseMembersDto::new);
        });
    }

}
