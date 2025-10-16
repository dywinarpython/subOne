package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticsSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface AnalyticSubscriptionService {
    Mono<ResponseAnalyticsSubscriptionDto> getAnalyticById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<ResponseTotalAnalyticSubscriptionGroupDto> getAlreadyPaidByGroupId(Long groupId, Jwt jwt);
    Mono<Void> generateAnalyticSubscription(UserSubscription userSubscription);
}
