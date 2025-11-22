package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticGroupsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

public interface AnalyticSubscriptionService {
    Mono<ResponseTotalAnalyticSubscriptionDto> getTotalAnalyticById(Long groupId, Long subscriptionId, Jwt jwt);
    Mono<ResponseAnalyticPaymentSubscriptionsDto> getPaymentInfoSubscriptionById(Long groupId, Long subscriptionId, Integer page, Jwt jwt);
    Mono<ResponseTotalAnalyticSubscriptionGroupDto> getAlreadyPaidByGroupId(Long groupId, Jwt jwt);
    Mono<ResponseTotalAnalyticGroupsDto> getTotalAnalyticGroups(Jwt jwt);
    Mono<Void> generateAnalyticSubscription(UserSubscription userSubscription);
}
