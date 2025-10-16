package com.subOne.subscriptions_service.scheduler;

import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.repository.AnalyticSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.temporal.TemporalAmount;

@Slf4j
@Component
@RequiredArgsConstructor
public class  SchedulerAnalyticControl {

    private final AnalyticSubscriptionRepository analyticSubscriptionRepository;

    // TODO при выпуска в PROD меняем аналитику каждый день в полночь
    // TODO разобраться с количеством запросов
    // @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void generateSubscriptionsAnalytic() {
        analyticSubscriptionRepository.selectSubscriptionsLastDatePaid().flatMap(subscriptionLastDatePaymentDto -> {
            PaymentPeriod paymentPeriod = PaymentPeriod.valueOf(subscriptionLastDatePaymentDto.paymentPeriod());
            TemporalAmount date = paymentPeriod.generatePeriod();
            LocalDate lasDatePaid = subscriptionLastDatePaymentDto.datePaid();
            LocalDate nextDatePaid = lasDatePaid.plus(date);
            if(nextDatePaid.isBefore(LocalDate.now())){
                AnalyticSubscription analyticSubscription = new AnalyticSubscription();
                analyticSubscription.setDatePaid(nextDatePaid);
                analyticSubscription.setSubscriptionId(subscriptionLastDatePaymentDto.subscriptionId());
                analyticSubscription.setAmount(subscriptionLastDatePaymentDto.amount());
                return Mono.just(analyticSubscription);
            }
            return Mono.empty();
        }).buffer(20)
        .flatMap(analyticSubscriptions ->
                analyticSubscriptionRepository.saveAll(analyticSubscriptions).then()).subscribe();
    }
}
