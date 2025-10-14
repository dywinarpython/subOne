package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Tag(name = "Управление участниками группы")
@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GroupMemberService groupMemberService;




    @Operation(summary = "Получение членов группы")
    @GetMapping
    public Mono<ResponseEntity<ResponseMembersDto>> getMembers(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.getUsers(groupId, jwt).map(ResponseEntity::ok);
    }

    @Operation(summary = "Проверка пользователь член группы")
    @GetMapping("/check")
    public Mono<ResponseEntity<Map<String, Boolean>>> checkUserInGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.checkUserInGroup(groupId, jwt).thenReturn(ResponseEntity.ok(Map.of("hasAccess", true)));
    }

    @Operation(summary = "Проверка пользователь собственник группы")
    @GetMapping("/check/owner")
    public Mono<ResponseEntity<Map<String, Boolean>>> checkUserIsOwnerGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.checkUserIsOwnerGroup(groupId, jwt).thenReturn(ResponseEntity.ok(Map.of("hasAccess", true)));
    }

    @Operation(summary = "Удаление пользователя из группы")
    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteMember(
            @PathVariable Long groupId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.deleteMember(groupId, userId, jwt)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
