package com.subOne.subscriptions_service.kafka.kasfka_handler_impl;

import com.subOne.subscriptions_service.kafka.kasfka_handler.KafkaHandlerService;
import com.subOne.subscriptions_service.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaHandlerServiceImpl implements KafkaHandlerService {

    private final UserSubscriptionService userSubscriptionService;

    @Override
    @KafkaListener(topics = "delete_group", concurrency = "3")
    public void deleteSubscriptionsByGroup(Long groupId) {
        userSubscriptionService.deleteSubscriptionByGroupId(groupId)
                .doOnError(throwable -> log.warn(throwable.getMessage()))
                .onErrorResume(ex -> Mono.empty())
                .subscribe();
    }
}
