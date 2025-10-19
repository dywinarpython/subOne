package com.subOne.user_service.service_impl;

import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.mapper.MapperGroupInvite;
import com.subOne.user_service.repository.group_invite_repository.GroupInviteRepository;
import com.subOne.user_service.service.GroupInviteService;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupInviteServiceImpl implements GroupInviteService {

    private final GroupMemberService groupMemberService;

    private final GroupService groupService;

    private final MapperGroupInvite mapperGroupInvite;

    private final GroupInviteRepository groupInviteRepository;

    private final CacheService cacheService;

    @Override
    @Transactional
    public Mono<ResponseMembersDto> addUserByCode(UUID code, Jwt jwt) {
        return cacheService.getValue("CODE::" + code, Long.class)
                .switchIfEmpty(groupInviteRepository.findGroupIdByCode(code))
                .flatMap(groupId ->
                        groupMemberService.addUser(UUID.fromString(jwt.getSubject()), groupId))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.GONE, "Invite code has expired")));
    }


    @Override
    @Transactional
    public Mono<ResponseInviteCodeDto> getCodeByGroupId(Long groupId, Jwt jwt) {
        return groupService.checkUserIsOwner(groupId, jwt)
                .then(groupInviteRepository.findCodeByGroupId(groupId))
                .flatMap(codeDto -> {
                    long minutesExpiresAt =  Duration.between(LocalDateTime.now(), codeDto.expiresAt()).toSeconds();
                    return  Mono.just(new ResponseInviteCodeDto(codeDto.code(), minutesExpiresAt));
                })
                .switchIfEmpty(Mono.defer(() -> createCodeInvite(groupId)));
    }


    private Mono<ResponseInviteCodeDto> createCodeInvite(Long groupId) {
        return groupInviteRepository.save(mapperGroupInvite.codeAndGroupIdToGroupInvite(groupId, UUID.randomUUID()))
                .map(groupInvite ->
                        new ResponseInviteCodeDto(groupInvite.getCode(), Duration.ofMinutes(5).toSeconds()))
                .flatMap(code ->
                        cacheService.saveValue("CODE::" + code.code(), groupId, Duration.ofMinutes(5)).thenReturn(code));
    }
}
