package com.subOne.notifications_service.service;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notifications_service.dto.notification.request.RequestUpdateNotificationsDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsCountDto;
import com.subOne.notifications_service.dto.notification.response.ResponseNotificationsDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public interface NotificationService {
    ResponseNotificationsDto findNotReadNotificationsByUserId(Jwt jwt, Integer page);
    ResponseNotificationsDto findReadNotificationsByUserId(Jwt jwt, Integer page);
    ResponseNotificationsCountDto findCountNotReadNotifications(Jwt jwt);
    void saveNotification(ConsumerRecord<UUID, SendNotificationDto> record);
    void saveNotificationCreateUser(UserInfo userInfo);
    void saveNotificationPaymentSubscription(KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription);
    void readNotification(RequestUpdateNotificationsDto requestUpdateNotificationsDto, Jwt jwt);
}
