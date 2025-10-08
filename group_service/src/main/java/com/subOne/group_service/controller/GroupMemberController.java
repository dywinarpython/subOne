package com.subOne.group_service.controller;

import com.subOne.group_service.dto.group_member.request.RequestDeleteMemberDto;
import com.subOne.group_service.dto.group.response.ResponseGroupDto;
import com.subOne.group_service.dto.group_member.response.ResponseMembersDto;
import com.subOne.group_service.service.GroupMemberService;
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

import java.util.UUID;

@Tag(name = "Управление участниками группы")
@RestController
@RequestMapping("/api/v1/group/member")
@RequiredArgsConstructor
public class GroupMemberController {


    private final GroupMemberService groupMemberService;

    @Operation(
            summary = "Добавление пользователя в группу",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseGroupDto.class))
                    )
            )
    )
    @PostMapping("/join")
    public Mono<ResponseEntity<ResponseMembersDto>> addMember(@RequestParam UUID code, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.addUser(code, jwt).map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Operation(
            summary = "Получение членов группы",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseMembersDto.class))
                    )
            )
    )
    @GetMapping("/{groupId}")
    public Mono<ResponseEntity<ResponseMembersDto>> getMembers(@PathVariable Long groupId, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.getUsers(groupId, jwt).map(ResponseEntity::ok);
    }


    @Operation(
            summary = "Удаления пользователя из группы"
    )
    @DeleteMapping
    public Mono<ResponseEntity<Void>> deleteGroup(@Valid @RequestBody Mono<RequestDeleteMemberDto> requestDeleteMemberDto, @AuthenticationPrincipal Jwt jwt) {
        return groupMemberService.deleteUser(requestDeleteMemberDto, jwt).thenReturn(ResponseEntity.noContent().build());
    }

}
