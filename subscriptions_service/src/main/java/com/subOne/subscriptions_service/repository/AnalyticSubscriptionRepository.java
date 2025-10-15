package com.subOne.subscriptions_service.repository;

import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.repository.select.SelectLastDatePaidRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AnalyticSubscriptionRepository extends R2dbcRepository<AnalyticSubscription, Long>, SelectLastDatePaidRepository {

    @Query("""
            select u.id, a.date_paid, sum(u.amount) as alreadyPaid
            from analytic_subscriptions a
            join user_subscriptions u ON a.subscription_id = u.id
            where u.id = :subscriptionId
            GROUP BY u.id, a.date_paid
            """)
    Mono<ResponseAnalyticSubscriptionDto> findBySubscriptionId(Long subscriptionId);

    @Query(
            """
            select SUM(u.amount)
            from analytic_subscriptions a
            join user_subscriptions u ON a.subscription_id = u.id
            where u.group_id = :groupId
            """
    )
    Mono<BigDecimal> findAllAlreadyPaidByGroupId(Long groupId);

}
