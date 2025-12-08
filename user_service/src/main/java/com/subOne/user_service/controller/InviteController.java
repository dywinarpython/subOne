package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group_invite.response.ResponseInviteCodeDto;
import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.service.GroupInviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;


@Tag(name = "Управление приглашениями")
@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InviteController {

    private final GroupInviteService groupInviteService;

    @Operation(summary = "Присоединение к группе по коду приглашения")
    @PostMapping("/join")
    public Mono<ResponseEntity<ResponseMembersDto>> joinGroupByCode(@RequestParam UUID code, @AuthenticationPrincipal Jwt jwt) {
        return groupInviteService.addUserByCode(code, jwt)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Operation(summary = "Получения кода приглашения")
    @GetMapping("/{groupId}")
    public Mono<ResponseInviteCodeDto> getCodeByGroupId(@PathVariable Integer groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupInviteService.getCodeByGroupId(groupId, jwt);
    }

}
