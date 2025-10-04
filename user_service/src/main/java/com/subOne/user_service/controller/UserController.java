package com.subOne.user_service.controller;

import com.subOne.user_service.dto.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.dto.response.UsersResponseDto;
import com.subOne.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Управление пользователями")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получение пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))
                    )
            )
    )
    @GetMapping("/{id}")
    public Mono<UserResponseDto> getUserById(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUserById(UUID.fromString(jwt.getSubject()));
    }

    @Operation(
            summary = "Получение пользователей",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = UsersResponseDto.class))
                    )
            )
    )
    @GetMapping
    public Mono<UsersResponseDto> getUserById(@RequestParam("usersId") List<UUID> usersId) {
        return userService.getUsersById(usersId);
    }


    @Operation(
            summary = "Обновление информации пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = RequestUpdateUserDto.class))
                    )
            )
    )
    @PatchMapping
    public Mono<ResponseEntity<Map<String, String>>> getUserById(@Valid @RequestBody Mono<RequestUpdateUserDto> updateUser, @AuthenticationPrincipal Jwt jwt) {
        return userService.updateUser(updateUser, jwt).thenReturn(ResponseEntity.ok(Map.of("message", "user is updated")));
    }

}
