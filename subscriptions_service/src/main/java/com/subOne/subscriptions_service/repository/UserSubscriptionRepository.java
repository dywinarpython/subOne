package com.subOne.subscriptions_service.repository;

import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.repository.update.UpdateRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public interface UserSubscriptionRepository extends R2dbcRepository<UserSubscription, Long>, UpdateRepository {

    Flux<ResponseSubscriptionDto> findByGroupId(Long groupId);

    @Query("""
            select id, service_name, subscription_name, start_date, end_date, payment_period, amount, status
            from user_subscriptions
            where id = :id
            """)
    Mono<ResponseSubscriptionDto> findBySubscriptionId(Long id);
}
