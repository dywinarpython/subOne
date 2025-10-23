package com.subOne.subscriptions_service.controller;


import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.dto.analytic_subscription.response.ResponseTotalAnalyticSubscriptionGroupDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.entity.enumEntity.SubscriptionStatus;
import com.subOne.subscriptions_service.mapper.UserSubscriptionMapper;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.AnalyticSubscriptionRepository;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class UserSubscriptionControllerTest extends AbstractControllerTest{

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private AnalyticSubscriptionRepository analyticSubscriptionRepository;

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper;

    @Autowired
    protected CacheService cacheService;

    protected UserSubscription userSubscription;

    protected String URI;

    @BeforeEach
    void setUp(){
        this.userSubscription = createSubscription();
        URI = "/api/v1/groups/" + userSubscription.getGroupId() + "/subscriptions";
    }

    private void checkSubscription(ResponseSubscriptionDto dto){
        assertEquals(dto.id(), userSubscription.getId());
        assertEquals(0, dto.amount().compareTo(userSubscription.getAmount()));
        assertEquals(dto.subscriptionName(), userSubscription.getSubscriptionName());
        assertEquals(dto.serviceName(), userSubscription.getServiceName());
        assertEquals(dto.status(), userSubscription.getStatus());
        assertEquals(dto.startDate(), userSubscription.getStartDate());
        assertEquals(dto.endDate(), userSubscription.getEndDate());
        assertEquals(dto.updatedAt(), userSubscription.getUpdatedAt());
        assertEquals(dto.paymentPeriod(), userSubscription.getPaymentPeriod());
    }
    private void checkSubscription(ResponseSubscriptionDto dto, UserSubscription userSubscription){
        assertEquals(dto.id(), userSubscription.getId());
        assertEquals(0, dto.amount().compareTo(userSubscription.getAmount()));
        assertEquals(dto.subscriptionName(), userSubscription.getSubscriptionName());
        assertEquals(dto.serviceName(), userSubscription.getServiceName());
        assertEquals(dto.status(), userSubscription.getStatus());
        assertEquals(dto.startDate(), userSubscription.getStartDate());
        assertEquals(dto.endDate(), userSubscription.getEndDate());
        assertEquals(dto.updatedAt(), userSubscription.getUpdatedAt());
        assertEquals(dto.paymentPeriod(), userSubscription.getPaymentPeriod());
    }

    private UserSubscription createSubscription(){
        UserSubscription userSubscription1 = new UserSubscription();
        userSubscription1.setPaymentPeriod(PaymentPeriod.MONTHLY.toString());
        userSubscription1.setSubscriptionName("name");
        userSubscription1.setAmount(BigDecimal.valueOf(1000));
        userSubscription1.setServiceName("serviceName");
        userSubscription1.setEndDate(LocalDate.now().plusMonths(3));
        userSubscription1.setStartDate(LocalDate.now().minusMonths(3));
        userSubscription1.setGroupId(1L + System.currentTimeMillis());
        userSubscription1.setStatus(SubscriptionStatus.ACTIVE.toString());
        return userSubscriptionRepository.save(userSubscription1).block();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/subscriptions?page=")
    void getSubscriptionsGroup_GroupFoundAndSubscriptionsFound_CorrectReturn(){
        Flux<ResponseSubscriptionsDto> result = webTestClient.get()
                .uri(URI + "?page=" + 0)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseSubscriptionsDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(subscriptionsDto -> subscriptionsDto.subscriptions().forEach(this::checkSubscription))
                .verifyComplete();
    }

    @Test
    @DisplayName("GET -> /api/v1/groups/{groupId}/subscriptions/{subscriptionId}")
    void getSubscriptionById_GroupFoundAndSubscriptionsFound_CorrectReturn(){
        Flux<ResponseSubscriptionDto> result = webTestClient.get()
                .uri(URI + "/" + userSubscription.getId())
                .exchange()
                .expectStatus().isOk()
                .returnResult(ResponseSubscriptionDto.class).getResponseBody();

        StepVerifier.create(result)
                .assertNext(this::checkSubscription)
                .verifyComplete();
    }


    @Test
    @DisplayName("POST -> /api/v1/groups/{groupId}/subscriptions")
    void saveSubscription_GroupFound_CorrectReturnAndCheckRepoSave() {
        String key = "ANALYTIC_GROUP::" + userSubscription.getGroupId();
        int count = 15;
        StepVerifier.create(cacheService.saveValue(key,
                        new ResponseTotalAnalyticSubscriptionGroupDto(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TWO),
                        Duration.ofMinutes(1)))
                .verifyComplete();
        RequestSubscriptionDto requestSubscriptionDto = new RequestSubscriptionDto(
                "ServiceName",
                "name",
                LocalDate.now().minusMonths(count),
                LocalDate.now().plusMonths(count),
                PaymentPeriod.MONTHLY,
                BigDecimal.valueOf(1000)
        );
        UserSubscription userSubscriptionCheck = userSubscriptionMapper.requestSubscriptionDtoToUserSubscription(requestSubscriptionDto, userSubscription.getGroupId());

        Flux<ResponseSubscriptionDto> result = webTestClient.post()
                .uri(URI)
                .bodyValue(requestSubscriptionDto)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(ResponseSubscriptionDto.class).getResponseBody();
        StepVerifier.create(result)
                .assertNext(subscriptionDto -> {
                    userSubscriptionCheck.setId(subscriptionDto.id());
                    checkSubscription(subscriptionDto, userSubscriptionCheck);
                    StepVerifier.create(analyticSubscriptionRepository.findBySubscriptionId(subscriptionDto.id(), PageRequest.of(0, count)).collectList())
                            .assertNext(ls -> assertEquals(count, ls.size()))
                            .verifyComplete();
                })
                .verifyComplete();
        StepVerifier.create(cacheService.getValue(key, ResponseTotalAnalyticSubscriptionGroupDto.class))
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    @DisplayName("PATCH -> /api/v1/groups/{groupId}/subscriptions/{subscriptionId}")
    void updateSubscription_GroupFoundAndSubscriptionFound_CorrectUpdateAndCheckRepoUpdate(){
        RequestUpdateSubscriptionDto dto = new RequestUpdateSubscriptionDto(
                "serviceName1", "name1", BigDecimal.valueOf(500), true);
        webTestClient.patch()
                .uri(URI + "/" + userSubscription.getId())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk();

        StepVerifier.create(userSubscriptionRepository.findById(userSubscription.getId()))
                .assertNext(result -> {
                    assertEquals(0, dto.amount().compareTo(result.getAmount()));
                    assertEquals(dto.subscriptionName(), result.getSubscriptionName());
                    assertEquals(dto.serviceName(), result.getServiceName());
                    assertEquals(SubscriptionStatus.STOP.toString(), result.getStatus());
                    assertNotNull(result.getUpdatedAt());
                })
                .verifyComplete();
    }
    @Test
    @DisplayName("PATCH /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/extend/{extensionCount}")
    void extendSubscription_GroupFoundAndExtensionCountCorrect_CheckRepoUpdate(){
        int extensionCount = 2;
        webTestClient.patch()
                    .uri(URI + "/" + userSubscription.getId() + "/extend/" + extensionCount)
                    .exchange()
                    .expectStatus().isOk();

        StepVerifier.create(userSubscriptionRepository.findById(userSubscription.getId()))
                    .assertNext(result -> {
                            PaymentPeriod paymentPeriod = PaymentPeriod.valueOf(result.getPaymentPeriod());
                            if (paymentPeriod.equals(PaymentPeriod.MONTHLY)){
                                assertEquals(userSubscription.getEndDate().plusMonths(extensionCount), result.getEndDate());
                            } else {
                                assertEquals(userSubscription.getEndDate().plusYears(extensionCount), result.getEndDate());
                            }
                    })
                    .verifyComplete();
    }
    @Test
    @DisplayName("DELETE -> /api/v1/groups/{groupId}/subscriptions/{subscriptionId}")
    void deleteSubscription_GroupFoundAndSubscriptionFound_CheckRepoDelete(){
        webTestClient.delete()
                .uri(URI + "/" + userSubscription.getId())
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(userSubscriptionRepository.existsById(userSubscription.getId()))
                .assertNext(Assertions::assertFalse)
                .verifyComplete();
    }




}
