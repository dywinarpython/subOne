package com.subOne.notifications_service.dto.notification.response;

import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;

import java.time.OffsetDateTime;

public record ResponseNotificationDto(Integer id, NotificationTargetType notificationTargetType, NotificationType notificationType, Integer targetId, OffsetDateTime createdAt) {
}
