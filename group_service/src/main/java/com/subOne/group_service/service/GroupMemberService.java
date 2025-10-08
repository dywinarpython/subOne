package com.subOne.group_service.service;

import com.subOne.group_service.dto.group_member.request.RequestDeleteMemberDto;
import com.subOne.group_service.dto.group_member.response.ResponseMembersDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupMemberService {
    Mono<ResponseMembersDto> addUser(UUID codeVerify, Jwt jwt);
    Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt);
    Mono<Void> deleteUser(Mono<RequestDeleteMemberDto> requestDeleteUserDtoMono, Jwt jwt);



}
