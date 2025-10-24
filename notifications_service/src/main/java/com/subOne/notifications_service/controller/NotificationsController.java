package com.subOne.notifications_service.controller;

import com.subOne.notifications_service.dto.response.ResponseNotificationsDto;
import com.subOne.notifications_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Получение уведомления пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseNotificationsDto.class)
                    )
            )
    )
    @GetMapping
    public ResponseNotificationsDto getNotifications(@AuthenticationPrincipal Jwt jwt, @RequestParam Integer page){
        return notificationService.findByUserId(jwt, page);
    }
}
