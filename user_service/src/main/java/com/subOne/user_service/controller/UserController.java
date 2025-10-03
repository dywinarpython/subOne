package com.subOne.user_service.controller;

import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Управление пользователями")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Получение пользователя по id",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "Список идентификаторов",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))
                    )
            )
    )
    @GetMapping("/{id}")
    public Mono<UserResponseDto> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

}
