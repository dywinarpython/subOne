package com.subOne.user_service.controller;

import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.dto.user.response.ResponseVerifyEmailDto;
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

import java.util.Map;
import java.util.UUID;

@Tag(name = "Управление пользователями")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получение пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseUserDto.class))
                    )
            )
    )
    @GetMapping("/me")
    public Mono<ResponseUserDto> getUserById(@AuthenticationPrincipal Jwt jwt) {
        return userService.getUserById(jwt);
    }


    @Operation(
            summary = "Обновление информации пользователя"
    )
    @PatchMapping("/me")
    public Mono<ResponseEntity<Map<String, String>>> updateUser(@Valid @RequestBody Mono<RequestUpdateUserDto> updateUser, @AuthenticationPrincipal Jwt jwt) {
        return userService.updateUser(updateUser, jwt).thenReturn(ResponseEntity.ok(Map.of("message", "user is updated")));
    }


    @Operation(
            summary = "Удаления пользователя"
    )
    @DeleteMapping("/me")
    public Mono<ResponseEntity<Void>> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        return userService.deleteUser(jwt).thenReturn(ResponseEntity.noContent().build());
    }

    @Operation(
            summary = "Проверка верификации почты пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = ResponseVerifyEmailDto.class))
                    )
            )
    )
    @GetMapping("/me/verify_email")
    public Mono<ResponseVerifyEmailDto> checkVerifyEmail(@AuthenticationPrincipal Jwt jwt) {
        return userService.checkVerifyEmail(UUID.fromString(jwt.getSubject())).map(ResponseVerifyEmailDto::new);
    }
}
