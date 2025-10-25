package com.subOne.notifications_service.controller;

import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import com.subOne.notifications_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Map;


@Tag(name = "Управление уведомлениями")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationsController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Получение непрочитанных уведомлений пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseNotificationsDto.class)
                    )
            )
    )
    @GetMapping("/new")
    public ResponseNotificationsDto getNewNotifications(@AuthenticationPrincipal Jwt jwt, @RequestParam Integer page){
        return notificationService.findNotReadNotificationsByUserId(jwt, page);
    }

    @Operation(
            summary = "Получение прочитанных уведомлений пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseNotificationsDto.class)
                    )
            )
    )
    @GetMapping("/old")
    public ResponseNotificationsDto getOldNotifications(@AuthenticationPrincipal Jwt jwt, @RequestParam Integer page){
        return notificationService.findReadNotificationsByUserId(jwt, page);
    }

    @Operation(
            summary = "Получение количество не прочитанных уведомлений пользователя",
            responses = @ApiResponse(
                    responseCode = "200",
                    content = @Content(
                            schema = @Schema(implementation = ResponseNotificationsCountDto.class)
                    )
            )
    )
    @GetMapping("/count")
    public ResponseNotificationsCountDto getCountNotifications(@AuthenticationPrincipal Jwt jwt){
        return notificationService.findCountNotReadNotifications(jwt);
    }

    @Operation(
            summary = "Обновление уведомлений (прочитать)"
    )
    @PatchMapping
    public ResponseEntity<Map<String, String>> getNotifications(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RequestUpdateNotificationsDto requestUpdateNotificationsDto){
        notificationService.readNotification(requestUpdateNotificationsDto, jwt);
        return ResponseEntity.ok(Map.of("message", "notification updated"));
    }
}
