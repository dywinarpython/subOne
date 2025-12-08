package com.subOne.subscriptions_service.kafka;

import com.subOne.subscriptions_service.BaseIntegrationTest;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.entity.enumEntity.SubscriptionStatus;
import com.subOne.subscriptions_service.kafka.kasfka_handler.KafkaHandlerService;
import com.subOne.subscriptions_service.kafka.producer.KafkaServiceImpl;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import com.subOne.subscriptions_service.service.AnalyticSubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.test.StepVerifier;

import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

public class KafkaHandlerServiceTest extends BaseIntegrationTest {

    @Autowired
    private KafkaHandlerService kafkaHandlerService;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private AnalyticSubscriptionService analyticSubscriptionService;

    @Autowired
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @MockitoBean
    private KafkaServiceImpl kafkaService;

    @Test
    void deleteSubscriptionsByGroup_GroupFound_CorrectDeleteAndCheckDeleteAnalytics(){
        int countAnalytic = 2;
        UserSubscription userSubscription = new UserSubscription();
        userSubscription.setGroupId(Math.abs( (int) System.currentTimeMillis()));
        userSubscription.setSubscriptionName("name");
        userSubscription.setServiceName("serviceName");
        userSubscription.setStartDate(LocalDate.now());
        userSubscription.setEndDate(LocalDate.now().plusMonths(countAnalytic));
        userSubscription.setPaymentPeriod(PaymentPeriod.MONTHLY.toString());
        userSubscription.setStatus(SubscriptionStatus.ACTIVE.toString());
        userSubscription.setAmount(BigDecimal.valueOf(299.99d));
        userSubscription = userSubscriptionRepository.save(userSubscription).block();
        analyticSubscriptionService.generateAnalyticSubscription(userSubscription).block();
        Assertions.assertNotNull(userSubscription);

        kafkaHandlerService.deleteSubscriptionsByGroup(userSubscription.getGroupId());

        UserSubscription finalUserSubscription = userSubscription;
        await().atMost(5, TimeUnit.SECONDS).untilAsserted( () -> {
            StepVerifier.create(userSubscriptionRepository.findByGroupId(finalUserSubscription.getGroupId(), PageRequest.of(0, 1000)))
                    .expectNextCount(0)
                    .verifyComplete();

            StepVerifier.create(analyticSubscriptionRepository.findBySubscriptionId(finalUserSubscription.getId(), PageRequest.of(0, 1000)))
                    .expectNextCount(0)
                    .verifyComplete();
        });
    }

}
