package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionsDto;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface UserSubscriptionService {
    Mono<ResponseSubscriptionDto> saveSubscription(Long groupId, Mono<RequestSubscriptionDto> requestSubscriptionDtoMono, Jwt jwt);
    Mono<Void> updateSubscription(Long groupId, Long subscriptionId, Mono<RequestUpdateSubscriptionDto> requestUpdateSubscriptionDtoMono, Jwt jwt);
    Mono<ResponseSubscriptionsDto> getSubscriptionsGroup(Long groupId, Jwt jwt);
    Mono<ResponseSubscriptionDto> getSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<Void> deleteSubscriptionById(Long groupId, Long subscriptionId, Jwt jwt);


}
