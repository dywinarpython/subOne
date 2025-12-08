package com.subOne.subscriptions_service.kafka.kasfka_handler;

public interface KafkaHandlerService {
    void deleteSubscriptionsByGroup(Integer groupId);
}
