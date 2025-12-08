package com.subOne.subscriptions_service.repository.analytic_subscription_repository.insert;

import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import reactor.core.publisher.Mono;

import java.util.List;

@FunctionalInterface
public interface InsertAnalyticSubscriptionRepository {
    Mono<Void> insertAllAnalyticSubscription(List<AnalyticSubscription> analyticSubscriptions);
}
