package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AnalyticSubscriptionService {
    Mono<ResponseAnalyticSubscriptionDto> getAnalyticById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<BigDecimal> getAlreadyPaidByGroupId(Long groupId, Jwt jwt);
    Mono<Void> generateAnalyticSubscription(UserSubscription userSubscription);
}
