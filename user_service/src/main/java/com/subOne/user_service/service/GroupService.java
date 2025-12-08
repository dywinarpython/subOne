package com.subOne.user_service.service;

import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupOwnerIdDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface GroupService {
    Mono<ResponseGroupDto> saveGroup(Mono<RequestGroupDto> requestGroupDtoMono, Jwt jwt);
    Mono<ResponseGroupDto> getGroupById(Integer groupId, Jwt jwt);
    Mono<ResponseUserDto> getOwner(Integer groupId);
    Mono<ResponseGroupOwnerIdDto> getOwnerId(Integer groupId);
    Mono<ResponseGroupsDto> getGroupsCreateUser(Jwt jwt);
    Mono<ResponseGroupsDto> getGroupsUserIsMember(Jwt jwt, Integer page);
    Mono<Integer> getCountGroup(Jwt jwt);
    Mono<Void> updateGroup(Mono<RequestUpdateGroupDto> requestGroupDtoMono, Integer groupId, Jwt jwt);
    Mono<Void> deleteGroup(Integer groupId, Jwt jwt);
    Mono<Boolean> checkUserIsOwner(Integer groupId, Jwt jwt);
    Mono<Void> checkUserIsOwnerGroups(List<Integer> groupsId, Jwt jwt);
    Mono<Boolean> checkUserIsOwnerWithoutCacheGet(Integer groupId, Jwt jwt);
    Mono<Void> deleteDataRelatedGroupsByOwnerId(Flux<Integer> groupsId);
    Flux<Integer> getGroupsIdByUserId(Jwt jwt);

}
