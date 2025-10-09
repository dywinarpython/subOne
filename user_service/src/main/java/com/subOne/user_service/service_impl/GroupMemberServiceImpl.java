package com.subOne.user_service.service_impl;

import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.mapper.MapperGroupMember;
import com.subOne.user_service.repository.GroupMemberRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {

    private final MapperGroupMember mapperGroupMember;

    private final GroupService groupService;

    private final GroupMemberRepository groupMemberRepository;


    @Override
    public Mono<ResponseMembersDto> addUser(UUID userId, Long groupId) {
        return groupMemberRepository.existsByUserIDAndGroupId(userId, groupId)
                .flatMap( bl -> {
                    if (bl) return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of the group"));
                    return Mono.empty();
                })
                .then(groupMemberRepository.existsMembersInGroupIsNoMoreFive(groupId))
                .flatMap(bl -> {
                    if (!bl) return Mono.error(new ValidationException("There can be no more than 5 members of the group (the owner is not considered)"));
                    return Mono.empty();
                })
                .then(groupMemberRepository.save(mapperGroupMember.userIdAndGroupIdToGroupMember(userId, groupId)))
                .then(getUsersAndOwner(groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt) {
        return checkUserInGroup(groupId, jwt)
                .then(getUsersAndOwner(groupId));
    }

    // TODO реализовать очистку кеша для удаленного пользователя
    @Override
    @Transactional
    public Mono<Void> deleteUser(Long groupId, UUID userId, Jwt jwt) {
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

    private Mono<ResponseMembersDto> getUsersAndOwner(Long groupId){
        return groupService.getOwnerId(groupId).flatMap( ownerId -> {
            Flux<ResponseMemberDto> members = groupMemberRepository.findByGroupId(groupId).map(id -> new ResponseMemberDto(id, false));
            return Flux.concat(
                    Flux.just(new ResponseMemberDto(ownerId, true)),
                    members
            ).collectList().map(ResponseMembersDto::new);
        });
    }

    private Mono<Void> checkUserInGroup(Long groupId, Jwt jwt){
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, UUID.fromString(jwt.getSubject())).flatMap(
                exists -> {
                    if(!exists) return groupService.checkUserIsOwner(groupId, jwt);
                    return Mono.empty();
                }
        );
    }

}
