package com.subOne.notifications_service.kafka.kafka_handlerImpl;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.keycloak_dto.UserInfo;
import com.subOne.notifications_service.kafka.kafka_handler.KafkaHandlerService;
import com.subOne.notifications_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
@RequiredArgsConstructor
public class KafkaHandlerServiceImpl implements KafkaHandlerService {
    private final NotificationService notificationService;


    @Override
    @KafkaListener(topics = "notification_user", concurrency = "3")
    public void saveNotification(ConsumerRecord<UUID, String> record) {
            notificationService.saveNotification(record);
    }

    @Override
    @KafkaListener(topics = "create_user", containerFactory = "userInfoKafkaListenerFactory")
    public void saveNotificationCreateUser(UserInfo userInfo) {
        notificationService.saveNotification(userInfo);
    }

    @Override
    @KafkaListener(topics = "payment_subscription", concurrency = "3", containerFactory = "paymentKafkaListenerFactory")
    public void saveNotificationPaymentSubscription(KafkaDtoPaymentSubscription kafkaDtoPaymentSubscription) {
        notificationService.saveNotificationPaymentSubscription(kafkaDtoPaymentSubscription);
    }

}
