package com.subOne.subscriptions_service.scheduler.unit;

import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentDto;
import com.subOne.subscriptions_service.dto.analytic_subscription.SubscriptionLastDatePaymentIdAndGroupIdDto;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.kafka.producer.KafkaService;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import com.subOne.subscriptions_service.scheduler.SchedulerSubscriptionControl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchedulerSubscriptionControlTest {

    @Mock
    private KafkaService kafkaService;

    @Mock
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private SchedulerSubscriptionControl schedulerSubscriptionControl;

    @Test
    void generateSubscriptionsAnalytic_CorrectGenerate(){
        List<SubscriptionLastDatePaymentDto> dtoList = List.of(
                new SubscriptionLastDatePaymentDto(1, PaymentPeriod.MONTHLY.toString(), BigDecimal.valueOf(100), LocalDate.now().minusMonths(1).minusDays(1)),
                new SubscriptionLastDatePaymentDto(1, PaymentPeriod.MONTHLY.toString(), BigDecimal.valueOf(100), LocalDate.now().minusMonths(1).minusDays(1))
        );
        when(cacheService.deleteValue(anyString())).thenReturn(Mono.empty());
        when(analyticSubscriptionRepository.selectSubscriptionsLastDatePaid()).thenReturn(Flux.fromIterable(dtoList));
        when(analyticSubscriptionRepository.insertAllAnalyticSubscription(any())).thenReturn(Mono.empty());

        schedulerSubscriptionControl.generateSubscriptionsAnalytic();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(cacheService, times(2)).deleteValue(anyString());
            verify(analyticSubscriptionRepository).selectSubscriptionsLastDatePaid();
            verify(analyticSubscriptionRepository).insertAllAnalyticSubscription(any());
        });
    }

    @Test
    void sendMessageWithPaymentInfo_CorrectUpdate(){
        when(userSubscriptionRepository.updateStatusByEndTime()).thenReturn(Mono.empty());

        schedulerSubscriptionControl.updateStatusSubscriptions();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> verify(userSubscriptionRepository).updateStatusByEndTime());
    }

    @Test
    void sendMessageWithPaymentInfo_CorrectSendMessage() {
        List<SubscriptionLastDatePaymentIdAndGroupIdDto> dtoList = List.of(
                new SubscriptionLastDatePaymentIdAndGroupIdDto(1, 1, PaymentPeriod.MONTHLY.toString(),  LocalDate.now().minusMonths(1)),
                new SubscriptionLastDatePaymentIdAndGroupIdDto(1, 1, PaymentPeriod.MONTHLY.toString(), LocalDate.now().minusMonths(1))
        );
        when(analyticSubscriptionRepository.selectSubscriptionsIdAndGroupByLastDatePaid()).thenReturn(Flux.fromIterable(dtoList));
        when(kafkaService.sendToTopic(anyString(), any())).thenReturn(Mono.empty());

        schedulerSubscriptionControl.sendMessageWithPaymentInfo();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                    verify(analyticSubscriptionRepository).selectSubscriptionsIdAndGroupByLastDatePaid();
                    verify(kafkaService, times(2)).sendToTopic(anyString(), any());
                }
        );
    }

    @Test
    void sendMessageWithAlreadyPaymentInfo_CorrectSendMessage(){
        List<SubscriptionLastDatePaymentIdAndGroupIdDto> dtoList = List.of(
                new SubscriptionLastDatePaymentIdAndGroupIdDto(1, 1, PaymentPeriod.MONTHLY.toString(),  LocalDate.now().minusDays(1)),
                new SubscriptionLastDatePaymentIdAndGroupIdDto(1, 1, PaymentPeriod.MONTHLY.toString(), LocalDate.now().minusDays(1))
        );
        when(analyticSubscriptionRepository.selectSubscriptionsIdAndGroupByLastDatePaid()).thenReturn(Flux.fromIterable(dtoList));
        when(kafkaService.sendToTopic(anyString(), any())).thenReturn(Mono.empty());

        schedulerSubscriptionControl.sendMessageWithAlreadyPaymentInfo();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
                    verify(analyticSubscriptionRepository).selectSubscriptionsIdAndGroupByLastDatePaid();
                    verify(kafkaService, times(2)).sendToTopic(anyString(), any());
                }
        );
    }
}



