package com.subOne.notifications_service.kafka.kafka_handler;

import com.subOne.keycloak_dto.UserInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

public interface KafkaHandlerService {
    void saveNotification(ConsumerRecord<UUID, String> notificationInfo);
    void saveNotificationCreateUser(UserInfo userInfo);
}
