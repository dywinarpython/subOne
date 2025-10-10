package com.subOne.user_service.service;

import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupMemberService {
    Mono<ResponseMembersDto> addUser(UUID userId, Long groupId);
    Mono<ResponseMembersDto> getUsers(Long groupId, Jwt jwt);
    Mono<Void> deleteUser(Long groupId, UUID userId, Jwt jwt);

}
