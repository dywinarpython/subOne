package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface UserSubscriptionService {
    Mono<ResponseSubscriptionDto> saveSubscription(Integer groupId, Mono<RequestSubscriptionDto> requestSubscriptionDtoMono, Jwt jwt);
    Mono<Void> updateSubscription(Integer groupId, Integer subscriptionId, Mono<RequestUpdateSubscriptionDto> requestUpdateSubscriptionDtoMono, Jwt jwt);
    Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(Integer groupId, Integer page,  Jwt jwt);
    Mono<ResponseSubscriptionDto> getSubscriptionById(Integer groupId, Integer subscriptionId, Jwt jwt);
    Mono<Void> deleteSubscriptionById(Integer groupId, Integer subscriptionId, Jwt jwt);
    Mono<Void> deleteSubscriptionsByGroupId(Integer groupId);
    Mono<Void> renewSubscriptionById(Integer groupId, Integer subscriptionId, Integer extensionCount, Jwt jwt);
}
