package com.subOne.subscriptions_service.repository.selectImpl;

import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import com.subOne.subscriptions_service.repository.select.SelectLastDatePaidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class SelectLastDatePaidRepositoryImpl implements SelectLastDatePaidRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Flux<SubscriptionLastDatePaymentDto> findSubscriptionsLastDatePaid() {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("""
                select a.subscription_id,
                       u.payment_period,
                       MAX(a.date_paid) as datePaid
                from analytic_subscriptions a
                join user_subscriptions u ON a.subscription_id = u.id
                where u.status not in ('STOP', 'DELETE')
                group by a.subscription_id, u.payment_period""")
                .map((row, metadata) -> new SubscriptionLastDatePaymentDto(
                        row.get("subscription_id", Long.class),
                        row.get("payment_period", String.class),
                        row.get("datePaid", java.time.LocalDate.class)
                ))
                .all();
    }
}

