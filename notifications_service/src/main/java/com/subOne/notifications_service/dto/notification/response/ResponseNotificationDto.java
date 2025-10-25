package com.subOne.notifications_service.dto.notification.response;

import java.time.OffsetDateTime;

public record ResponseNotificationDto(Long id, String message, OffsetDateTime createdAt) {
}
