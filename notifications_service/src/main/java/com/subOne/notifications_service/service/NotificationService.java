package com.subOne.notifications_service.service;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notifications_service.dto.response.ResponseNotificationsDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface NotificationService {
    ResponseNotificationsDto findByUserId(Jwt jwt, Integer page);
    void saveNotification(ConsumerRecord<UUID, String> record);
    void saveNotification(UserInfo userInfo);
}
