package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.user_service.dto.user.request.RequestGroupsOwnershipChangesDto;
import com.subOne.user_service.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @Operation(summary = "Получение членов группы")
    @GetMapping("/{groupId}/members")
    public Mono<ResponseEntity<ResponseMembersDto>> getMembers(@PathVariable Integer groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.getUsers(groupId, jwt).map(ResponseEntity::ok);
    }

    @Operation(summary = "Проверка пользователь член группы")
    @GetMapping("{groupId}/members/check")
    public Mono<ResponseEntity<Map<String, Boolean>>> checkUserInGroup(@PathVariable Integer groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.checkUserInGroup(groupId, jwt).thenReturn(ResponseEntity.ok(Map.of("hasAccess", true)));
    }

    @Operation(summary = "Проверка пользователь собственник группы")
    @GetMapping("{groupId}/members/check/owner")
    public Mono<ResponseEntity<Map<String, Boolean>>> checkUserIsOwnerGroup(@PathVariable Integer groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.checkUserIsOwnerGroup(groupId, jwt).thenReturn(ResponseEntity.ok(Map.of("hasAccess", true)));
    }

    @Operation(
            summary = "Изменения владельца группы/групп"
    )
    @PatchMapping("/change-owners")
    public Mono<ResponseEntity<Map<String, String>>> changesOwnerGroups(
            @Valid @RequestBody Mono<RequestGroupsOwnershipChangesDto> requestGroupsOwnershipChangesDtoMono,
            @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.changesOwnerGroup(requestGroupsOwnershipChangesDtoMono, jwt).thenReturn(ResponseEntity.ok(Map.of("message", "The owners of the group have been changed")));
    }

    @Operation(summary = "Удаление пользователя из группы")
    @DeleteMapping("{groupId}/members/{userId}")
    public Mono<ResponseEntity<Void>> deleteMember(
            @PathVariable Integer groupId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.deleteMember(groupId, userId, jwt)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
