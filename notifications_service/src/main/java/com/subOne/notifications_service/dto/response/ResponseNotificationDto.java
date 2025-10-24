package com.subOne.notifications_service.dto.response;

import java.time.OffsetDateTime;

public record ResponseNotificationDto(String message, OffsetDateTime createdAt) {
}
