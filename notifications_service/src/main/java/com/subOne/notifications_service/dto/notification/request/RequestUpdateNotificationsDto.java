package com.subOne.notifications_service.dto.notification.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RequestUpdateNotificationsDto(
        @NotNull(message = "ids not null") @Size(min = 1, max = 10, message = "Maximum of 10 notifications for updates and min of 1 notifications for updates") List<Long> ids) {
}
