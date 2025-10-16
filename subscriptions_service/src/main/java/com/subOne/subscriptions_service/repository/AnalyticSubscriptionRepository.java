package com.subOne.subscriptions_service.repository;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.repository.select.SelectAnalyticSubscriptionRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AnalyticSubscriptionRepository extends R2dbcRepository<AnalyticSubscription, Long>, SelectAnalyticSubscriptionRepository {

    // TODO добавить пагинацию
    Flux<ResponseAnalyticPaymentSubscriptionDto> findBySubscriptionId(Long subscriptionId);

}
