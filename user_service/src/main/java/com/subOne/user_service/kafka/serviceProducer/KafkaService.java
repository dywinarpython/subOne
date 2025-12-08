package com.subOne.user_service.kafka.serviceProducer;

import com.subOne.kafka_dto.SendNotificationDto;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface KafkaService {
    Mono<Void> sendToTopic(String nameTopic, String value);
    Mono<Void> sendToTopic(String nameTopic, Integer value);
    Mono<Void> sendToTopic(String nameTopic, UUID userId, SendNotificationDto kafkaDtoSendNotification);
}
