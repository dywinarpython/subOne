package com.subOne.subscriptions_service.scheduler;

import com.subOne.subscriptions_service.BaseIntegrationTest;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseAnalyticPaymentSubscriptionDto;
import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.entity.enumEntity.SubscriptionStatus;
import com.subOne.subscriptions_service.kafka.producer.KafkaServiceImpl;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SchedulerSubscriptionControlTest extends BaseIntegrationTest {

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @Autowired
    private SchedulerSubscriptionControl schedulerSubscriptionControl;

    @MockitoBean
    private KafkaServiceImpl kafkaService;


    private AnalyticSubscription analyticSubscription;

    private UserSubscription userSubscription;

    @BeforeEach
    void setUp(){
        userSubscription = new UserSubscription();
        userSubscription.setGroupId(Math.abs((int) System.currentTimeMillis()));
        userSubscription.setSubscriptionName("name");
        userSubscription.setServiceName("serviceName");
        userSubscription.setStartDate(LocalDate.now().minusMonths(2));
        userSubscription.setEndDate(LocalDate.now());
        userSubscription.setPaymentPeriod(PaymentPeriod.MONTHLY.toString());
        userSubscription.setStatus(SubscriptionStatus.ACTIVE.toString());
        userSubscription.setAmount(BigDecimal.valueOf(299.99d));
        userSubscription = userSubscriptionRepository.save(userSubscription).block();
        assertNotNull(userSubscription);
        analyticSubscription = new AnalyticSubscription();
        analyticSubscription.setAmount(userSubscription.getAmount());
        analyticSubscription.setSubscriptionId(userSubscription.getId());
        analyticSubscription.setDatePaid(userSubscription.getStartDate());
        analyticSubscription = analyticSubscriptionRepository.save(analyticSubscription).block();
        assertNotNull(analyticSubscription);

        lenient().when(kafkaService.sendToTopic(anyString(), any())).thenReturn(Mono.empty());
    }


    @Test
    void generateSubscriptionsAnalytic_FoundSubscriptionNotPayment_CorrectPaymentAndCheckRepo() {
        schedulerSubscriptionControl.generateSubscriptionsAnalytic();

        AnalyticSubscription finalAnalyticSubscription = analyticSubscription;
        UserSubscription finalUserSubscription = userSubscription;
        await().atMost(5, TimeUnit.SECONDS).untilAsserted( () -> StepVerifier.create(analyticSubscriptionRepository.findBySubscriptionId(finalUserSubscription.getId(), PageRequest.of(0, 10000)).collectList())
                .assertNext(list -> {
                    assertEquals(2, list.size());
                    ResponseAnalyticPaymentSubscriptionDto dto = list.getFirst();
                    assertEquals(dto.amount(), finalAnalyticSubscription.getAmount());
                    assertEquals(dto.datePaid(), finalAnalyticSubscription.getDatePaid());

                    dto = list.get(1);
                    assertEquals(dto.amount(), finalAnalyticSubscription.getAmount());
                    assertEquals(dto.datePaid(), finalAnalyticSubscription.getDatePaid().plusMonths(1));
                })
                .verifyComplete());
    }

    @Test
    void sendMessageWithAlreadyPaymentInfo_FindLastDate_CorrectSendMessage(){
        analyticSubscription.setDatePaid(LocalDate.now().minusDays(1));
        analyticSubscriptionRepository.save(analyticSubscription).block();

        schedulerSubscriptionControl.sendMessageWithAlreadyPaymentInfo();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                () -> verify(kafkaService, atLeastOnce()).sendToTopic(anyString(), any()));
    }




}
