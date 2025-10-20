package com.subOne.user_service.kafka.serviceProducer;

import reactor.core.publisher.Mono;

public interface KafkaService {
    Mono<Void> sendToTopic(String nameTopic, String value);
    Mono<Void> sendToTopic(String nameTopic, Long value);
}
