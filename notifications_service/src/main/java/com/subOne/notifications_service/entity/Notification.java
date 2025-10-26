package com.subOne.notifications_service.entity;

import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID userId;
    private OffsetDateTime createdAt;
    private Boolean read;
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    @Enumerated(EnumType.STRING)
    private NotificationTargetType notificationTargetType;
    private Long targetId;
}
