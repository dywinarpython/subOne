package com.subOne.user_service.controller;

import com.subOne.user_service.dto.group.request.RequestGroupDto;
import com.subOne.user_service.dto.group.request.RequestUpdateGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupDto;
import com.subOne.user_service.dto.group.response.ResponseGroupOwnerIdDto;
import com.subOne.user_service.dto.group.response.ResponseGroupsDto;
import com.subOne.user_service.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Tag(name = "Управление группами")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {


    private final GroupService groupService;

    @Operation(
            summary = "Создание группы",
            responses = @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            schema = @Schema(implementation = ResponseGroupDto.class)
                    )
            )
    )
    @PostMapping
    public Mono<ResponseEntity<ResponseGroupDto>> createGroup(@Valid @RequestBody Mono<RequestGroupDto> requestGroupDto, @AuthenticationPrincipal Jwt jwt) {
        return groupService.saveGroup(requestGroupDto, jwt).map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Operation(
            summary = "Получение группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseGroupDto.class))
                    )
            )
    )
    @GetMapping("/{groupId}")
    public Mono<ResponseGroupDto> getGroupById(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupService.getGroupById(groupId, jwt);
    }


    @Operation(
            summary = "Получение групп созданных пользователем",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseGroupsDto.class))
                    )
            )
    )
    @GetMapping("owner/me")
    public Mono<ResponseGroupsDto> getGroups(@AuthenticationPrincipal Jwt jwt) {
        return groupService.getGroupsCreateUser(jwt);
    }

    @Operation(
            summary = "Получение id собственника группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseGroupOwnerIdDto.class))
                    )
            )
    )
    @GetMapping("/{groupId}/owner")
    public Mono<ResponseGroupOwnerIdDto> getOwnerIdByGroupId(@PathVariable Long groupId) {
        return groupService.getOwnerId(groupId).map(ResponseGroupOwnerIdDto::new);
    }

    @Operation(
            summary = "Получение групп: пользователь член группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseGroupsDto.class))
                    )
            )
    )
    @GetMapping("/me")
    public Mono<ResponseGroupsDto> getGroupsUserIsMember(@RequestParam Integer page, @AuthenticationPrincipal Jwt jwt) {
        return groupService.getGroupsUserIsMember(jwt, page);
    }


    @Operation(
            summary = "Обновление информации группы"
    )
    @PatchMapping("/{groupId}")
    public Mono<ResponseEntity<Map<String, String>>> updateGroup(@PathVariable Long groupId, @Valid @RequestBody Mono<RequestUpdateGroupDto> requestUpdateGroupDtoMono , @AuthenticationPrincipal Jwt jwt) {
        return groupService.updateGroup(requestUpdateGroupDtoMono,groupId , jwt).thenReturn(ResponseEntity.ok(Map.of("message", "Group is updated")));
    }


    @Operation(
            summary = "Удаления группы"
    )
    @DeleteMapping("/{groupId}")
    public Mono<ResponseEntity<Void>> deleteGroup(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupService.deleteGroup(groupId, jwt).thenReturn(ResponseEntity.noContent().build());
    }

}
