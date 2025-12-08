package com.subOne.user_service.service;

import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.request.RequestGroupsOwnershipChangesDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupMemberService {
    Mono<ResponseMembersDto> addUser(UUID userId, Integer groupId);
    Mono<ResponseMembersDto> getUsers(Integer groupId, Jwt jwt);
    Mono<Void> deleteMember(Integer groupId, UUID userId, Jwt jwt);
    Mono<Boolean> checkUserInGroup(Integer groupId, Jwt jwt);
    Mono<Boolean> checkUserIsOwnerGroup(Integer groupId, Jwt jwt);
    Mono<Void> existsMemberInGroupByOwnerId(Jwt jwt);
    Mono<Void> changesOwnerGroup(Mono<RequestGroupsOwnershipChangesDto> requestGroupsOwnershipChangesDtoMono, Jwt jwt);

}
