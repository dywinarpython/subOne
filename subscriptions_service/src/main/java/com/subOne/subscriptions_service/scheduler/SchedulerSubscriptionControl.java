package com.subOne.subscriptions_service.scheduler;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.kafka.producer.KafkaService;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
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
public class  SchedulerSubscriptionControl {

    private final AnalyticSubscriptionRepository analyticSubscriptionRepository;

    private final UserSubscriptionRepository userSubscriptionRepository;

    private final CacheService cacheService;

    private final KafkaService kafkaService;


    // TODO при выпуска в PROD меняем аналитику каждый день в полночь + 10 minutes
    // @Scheduled(cron = "0 10 0 * * *")
    @Scheduled(cron = "2 * * * * *")
    public void generateSubscriptionsAnalytic() {
        analyticSubscriptionRepository.selectSubscriptionsLastDatePaid().flatMap(subscriptionLastDatePaymentDto -> {
                    PaymentPeriod paymentPeriod = PaymentPeriod.valueOf(subscriptionLastDatePaymentDto.paymentPeriod());
                    TemporalAmount date = paymentPeriod.generatePeriod();
                    LocalDate lasDatePaid = subscriptionLastDatePaymentDto.datePaid();
                    LocalDate nextDatePaid = lasDatePaid.plus(date);
                    if (nextDatePaid.isBefore(LocalDate.now())) {
                        AnalyticSubscription analyticSubscription = new AnalyticSubscription();
                        analyticSubscription.setDatePaid(nextDatePaid);
                        analyticSubscription.setSubscriptionId(subscriptionLastDatePaymentDto.subscriptionId());
                        analyticSubscription.setAmount(subscriptionLastDatePaymentDto.amount());
                        cacheService.deleteValue("ANALYTIC_SUBSCRIPTION::" + subscriptionLastDatePaymentDto.subscriptionId()).subscribe();
                        return Mono.just(analyticSubscription);
                    }
                    return Mono.empty();
                }
        ).buffer(30)
        .concatMap(analyticSubscriptionRepository::insertAllAnalyticSubscription).subscribe();
    }


    // TODO при выпуска в PROD меняем аналитику каждый день в полночь
    // @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void updateStatusSubscriptions() {
        userSubscriptionRepository.updateStatusByEndTime().subscribe();
    }

    @Scheduled(cron = "0 0 12 * * *")
    public void sendMessageWithPaymentInfo() {
        analyticSubscriptionRepository
                .selectSubscriptionsIdAndGroupByLastDatePaid()
                .filter(dto -> {
                    PaymentPeriod paymentPeriod = PaymentPeriod.valueOf(dto.paymentPeriod());
                    TemporalAmount period = paymentPeriod.generatePeriod();
                    LocalDate nextDatePaid = dto.datePaid().plus(period);
                    return nextDatePaid.isBefore(LocalDate.now().plusDays(2));})
                .doOnNext(dto -> kafkaService.sendToTopic("payment_subscription",
                        new KafkaDtoPaymentSubscription(dto.subscriptionId(), dto.groupId())).subscribe())
                .subscribe();
    }


}
