package com.subOne.notifications_service.dto.notification.response;

import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;

import java.time.OffsetDateTime;

public record ResponseNotificationDto(Long id, NotificationTargetType notificationTargetType, NotificationType notificationType, Long targetId, OffsetDateTime createdAt) {
}
