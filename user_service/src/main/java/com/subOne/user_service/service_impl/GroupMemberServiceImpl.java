package com.subOne.user_service.service_impl;

import com.subOne.user_service.dto.group_member.request.RequestDeleteMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMemberDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.repository.GroupMemberRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupService groupService;

    private final GroupMemberRepository groupMemberRepository;

    // TODO реализовать добавления пользователя через проверку кеша
    // TODO пригласить самого себя нельзя сделать проверку
    @Override
    @Transactional
    public Mono<ResponseMembersDto> addUser(UUID codeVerify, Jwt jwt) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt) {
        return checkUserInGroup(groupId, jwt)
                .then(groupMemberRepository.findOwnerByGroupId(groupId))
                .flatMap(ownerId -> {
                    Flux<ResponseMemberDto> members = groupMemberRepository.findByGroupId(groupId).map(id -> new ResponseMemberDto(id, false));
                    return Flux.concat(
                            Flux.just(new ResponseMemberDto(ownerId, true)),
                            members
                    ).collectList().map(ResponseMembersDto::new);
                });
    }

    // TODO реализовать очистку кеша для удаленного пользователя
    @Override
    @Transactional
    public Mono<Void> deleteUser(Mono<RequestDeleteMemberDto> requestDeleteUserDtoMono, Jwt jwt) {
        return requestDeleteUserDtoMono.flatMap(requestDeleteMemberDto ->
        {
            if (requestDeleteMemberDto.userId().equals(UUID.fromString(jwt.getSubject()))) { return Mono.error(new AccessDeniedException("The user cannot delete himself"));}
            return groupService.checkUserIsOwner(requestDeleteMemberDto.groupId(), jwt)
                    .then(groupMemberRepository.deleteByUserId(requestDeleteMemberDto.userId()))
                    .flatMap(count -> {
                        if (count == 0) return Mono.error(new NoSuchElementException("User is not found"));
                        return Mono.empty();
                    });
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
