package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface UserSubscriptionService {
    Mono<ResponseSubscriptionDto> saveSubscription(Long groupId, Mono<RequestSubscriptionDto> requestSubscriptionDtoMono, Jwt jwt);
    Mono<Void> updateSubscription(Long groupId, Long subscriptionId, Mono<RequestUpdateSubscriptionDto> requestUpdateSubscriptionDtoMono, Jwt jwt);
    Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(Long groupId, Jwt jwt);
    Mono<ResponseSubscriptionDto> getSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<Void> deleteSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<Void> renewSubscriptionById(Long groupId, Long subscriptionId, Long extensionCount, Jwt jwt);
}
