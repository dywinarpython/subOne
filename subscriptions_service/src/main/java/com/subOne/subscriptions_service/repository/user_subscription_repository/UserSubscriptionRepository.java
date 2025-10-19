package com.subOne.subscriptions_service.repository.user_subscription_repository;

import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.repository.user_subscription_repository.update.UpdateRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public interface UserSubscriptionRepository extends R2dbcRepository<UserSubscription, Long>, UpdateRepository {

    Flux<ResponseSubscriptionDto> findByGroupId(Long groupId, Pageable pageable);

    @Query("""
            select id, service_name, subscription_name, start_date, end_date, payment_period, amount, status
            from user_subscriptions
            where id = :id
            """)
    Mono<ResponseSubscriptionDto> findBySubscriptionId(Long id);

    @Modifying
    @Query("""
        update user_subscriptions
        set end_date = CASE payment_period
            when 'MONTHLY' then end_date + (:extension_count || ' month')::interval
            when 'YEARLY' then end_date + (:extension_count || ' year')::interval
        end
        where id = :subscriptionId
    """)
    Mono<Integer> updateEndTimeSubscriptionById(Long subscriptionId, Long extensionCount);

    @Modifying
    @Query("""
        update user_subscriptions
        set status = 'EXPIRED'
        where end_date < NOW() AND status <> 'EXPIRED'
    """)
    Mono<Integer> updateStatusByEndTime();
}
