package com.subOne.notifications_service.kafka.kafka_handler;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

public interface KafkaHandlerService {
    void saveNotification(ConsumerRecord<UUID, SendNotificationDto> notificationInfo);
    void saveNotificationCreateUser(UserInfo userInfo);
    void saveNotificationPaymentSubscription(KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription);
}
