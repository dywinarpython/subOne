package com.subOne.user_service.service;

import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupService {
    Mono<ResponseGroupDto> saveGroup(Mono<RequestGroupDto> requestGroupDtoMono, Jwt jwt);
    Mono<ResponseGroupDto> getGroupById(Long groupId, Jwt jwt);
    Mono<UUID> getOwnerId(Long groupId);
    Mono<ResponseGroupsDto> getGroups(Jwt jwt);
    Mono<Void> updateGroup(Mono<RequestUpdateGroupDto> requestGroupDtoMono, Long groupId, Jwt jwt);
    Mono<Void> deleteGroup(Long groupId, Jwt jwt);
    Mono<Void> checkUserIsOwner(Long groupId, Jwt jwt);
}
