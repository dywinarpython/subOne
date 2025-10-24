package com.subOne.user_service.kafka.serviceProducer;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface KafkaService {
    Mono<Void> sendToTopic(String nameTopic, String value);
    Mono<Void> sendToTopic(String nameTopic, Long value);
    Mono<Void> sendToTopic(String nameTopic, UUID userId, String message);
}
