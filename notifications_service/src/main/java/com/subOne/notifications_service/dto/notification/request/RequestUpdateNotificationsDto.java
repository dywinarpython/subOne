package com.subOne.notifications_service.dto.notification.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record RequestUpdateNotificationsDto(
        @Size(max = 10, message = "Maximum of 10 notifications for updates") List<Long> ids) {
}
