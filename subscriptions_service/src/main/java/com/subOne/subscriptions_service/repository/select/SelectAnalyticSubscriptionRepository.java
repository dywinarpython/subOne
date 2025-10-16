package com.subOne.subscriptions_service.repository.select;

import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SelectAnalyticSubscriptionRepository {
    Flux<SubscriptionLastDatePaymentDto> selectSubscriptionsLastDatePaid();
    Mono<ResponseTotalAnalyticSubscriptionDto> selectSumAmountAndLastDateBySubscriptionId(Long subscriptionId);
    Mono<ResponseTotalAnalyticSubscriptionGroupDto> selectTotalAnalyticByGroupId(Long groupId);

}
