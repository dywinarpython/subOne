package com.subOne.user_service.service;


import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupInviteService {

    Mono<ResponseMembersDto> addUserByCode(UUID code, Jwt jwt);


    Mono<ResponseInviteCodeDto> getCodeByGroupId(Long groupId, Jwt jwt);
}
