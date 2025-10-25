package com.subOne.subscriptions_service.kafka.producer;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import reactor.core.publisher.Mono;

public interface KafkaService {
    Mono<Void> sendToTopic(String nameTopic, KafkaDtoPaymentSubscription dto);
}
