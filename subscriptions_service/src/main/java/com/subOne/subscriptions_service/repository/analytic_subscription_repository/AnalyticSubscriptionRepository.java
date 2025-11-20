package com.subOne.subscriptions_service.repository.analytic_subscription_repository;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.insert.InsertAnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.select.SelectAnalyticSubscriptionRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface AnalyticSubscriptionRepository extends R2dbcRepository<AnalyticSubscription, Long>,
        SelectAnalyticSubscriptionRepository,
        InsertAnalyticSubscriptionRepository {

    Flux<ResponseAnalyticPaymentSubscriptionDto> findBySubscriptionId(Long subscriptionId, Pageable pageable);

}
