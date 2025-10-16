package com.subOne.subscriptions_service.repository.selectImpl;

import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.repository.select.SelectAnalyticSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class SelectAnalyticSubscriptionRepositoryImpl implements SelectAnalyticSubscriptionRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Flux<SubscriptionLastDatePaymentDto> selectSubscriptionsLastDatePaid() {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("""
                select a.subscription_id,
                       u.payment_period,
                       u.amount,
                       max(a.date_paid) as datePaid
                from analytic_subscriptions a
                join user_subscriptions u ON a.subscription_id = u.id
                where u.status not in ('STOP', 'DELETE')
                group by a.subscription_id, u.payment_period, u.amount""")
                .map((row, metadata) -> new SubscriptionLastDatePaymentDto(
                        row.get("subscription_id", Long.class),
                        row.get("payment_period", String.class),
                        row.get("amount", BigDecimal.class),
                        row.get("datePaid", LocalDate.class)
                ))
                .all();
    }

    @Override
    public Mono<ResponseTotalAnalyticSubscriptionDto> selectSumAmountAndLastDateBySubscriptionId(Long subscriptionId) {
        return r2dbcEntityTemplate.getDatabaseClient().sql(
                """
                select sum(amount) as alreadyPaid, max(date_paid) as lastDatePaid
                from analytic_subscriptions
                where subscription_id = :subscriptionId
                group by subscription_id""")
                .bind("subscriptionId", subscriptionId)
                .map((row, metadata) -> new ResponseTotalAnalyticSubscriptionDto(
                        row.get("alreadyPaid", BigDecimal.class),
                        row.get("lastDatePaid", LocalDate.class)
                ))
                .one();
    }

    @Override
    public Mono<ResponseTotalAnalyticSubscriptionGroupDto> selectTotalAnalyticByGroupId(Long groupId) {
        return r2dbcEntityTemplate.getDatabaseClient().sql("""
                        with analytic as (
                            select u.amount as amount, u.payment_period, sum(a.amount) as sum_amount
                            from analytic_subscriptions a
                            join user_subscriptions u on a.subscription_id = u.id
                            where u.group_id = :groupId
                            group by u.group_id, u.id, u.amount, u.payment_period
                            having extract(year from max(a.date_paid)) = extract(year from current_date)
                        )
                        select
                            sum(case when payment_period = 'MONTHLY' then amount else amount / 12.0 end) as monthPaid,
                            sum(sum_amount) as totalSum
                        from analytic""")
                .bind("groupId", groupId)
                .map((row, metadata) -> new ResponseTotalAnalyticSubscriptionGroupDto(
                        row.get("totalSum", BigDecimal.class),
                        row.get("monthPaid", BigDecimal.class),
                        null
                ))
                .one();
    }
}

