package com.subOne.subscriptions_service.repository.analytic_subscription_repository.select;

import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentIdAndGroupIdDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticGroupsDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SelectAnalyticSubscriptionRepository {
    Flux<SubscriptionLastDatePaymentDto> selectSubscriptionsLastDatePaid();
    Flux<SubscriptionLastDatePaymentIdAndGroupIdDto> selectSubscriptionsIdAndGroupByLastDatePaid();
    Mono<ResponseTotalAnalyticSubscriptionDto> selectSumAmountAndLastDateBySubscriptionId(Integer subscriptionId);
    Mono<ResponseTotalAnalyticSubscriptionGroupDto> selectTotalAnalyticByGroupId(Integer groupId);
    Mono<ResponseTotalAnalyticGroupsDto> selectTotalAnalyticByGroupsId(List<Integer> ids);

}
