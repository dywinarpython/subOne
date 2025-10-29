package com.subOne.subscriptions_service.scheduler;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.notification.NotificationType;
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


    @Scheduled(cron = "0 10 0 * * *")
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


    @Scheduled(cron = "0 0 0 * * *")
    public void updateStatusSubscriptions() {
        userSubscriptionRepository.updateStatusByEndTime().subscribe();
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendMessageWithPaymentInfo() {
        analyticSubscriptionRepository
                .selectSubscriptionsIdAndGroupByLastDatePaid()
                .filter(dto -> {
                    PaymentPeriod paymentPeriod = PaymentPeriod.valueOf(dto.paymentPeriod());
                    TemporalAmount period = paymentPeriod.generatePeriod();
                    LocalDate nextDatePaid = dto.datePaid().plus(period);
                    return nextDatePaid.isBefore(LocalDate.now().plusDays(2));
                })
                .doOnNext(dto -> kafkaService.sendToTopic("payment_subscription",
                        new KafkaDtoPaymentSubscription(dto.subscriptionId(), dto.groupId(), NotificationType.PAYEMNT_SUBSCRIPTION)).subscribe())
                .subscribe();
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void sendMessageWithAlreadyPaymentInfo() {
        analyticSubscriptionRepository
                .selectSubscriptionsIdAndGroupByLastDatePaid()
                .filter(dto -> dto.datePaid().plusDays(1).equals(LocalDate.now()))
                .doOnNext(dto -> kafkaService.sendToTopic("payment_subscription",
                        new KafkaDtoPaymentSubscription(dto.subscriptionId(), dto.groupId(), NotificationType.ALREADY_PAYEMNT_SUBS)).subscribe())
                .subscribe();
    }



}
