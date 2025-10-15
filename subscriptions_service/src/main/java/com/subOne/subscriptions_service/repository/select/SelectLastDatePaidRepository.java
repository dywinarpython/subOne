package com.subOne.subscriptions_service.repository.select;

import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import reactor.core.publisher.Flux;

public interface SelectLastDatePaidRepository {
    Flux<SubscriptionLastDatePaymentDto> findSubscriptionsLastDatePaid();
}
