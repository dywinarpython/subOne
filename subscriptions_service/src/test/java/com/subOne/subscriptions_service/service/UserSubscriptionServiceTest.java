package com.subOne.subscriptions_service.service;

import com.subOne.subscriptions_service.cache.CacheService;
import com.subOne.subscriptions_service.client.WebClientService;
import com.subOne.subscriptions_service.dto.subscription.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.dto.subscription.response.ResponseSubscriptionsDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.PaymentPeriod;
import com.subOne.subscriptions_service.entity.enumEntity.SubscriptionStatus;
import com.subOne.subscriptions_service.mapper.UserSubscriptionMapper;
import com.subOne.subscriptions_service.repository.user_subscription_repository.UserSubscriptionRepository;
import com.subOne.subscriptions_service.service_impl.UserSubscriptionServiceImpl;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;

    @Mock
    private AnalyticSubscriptionService analyticSubscriptionService;

    @Mock
    private UserSubscriptionMapper userSubscriptionMapper;

    @Mock
    private WebClientService webClientService;

    @Mock
    private CacheService cacheService;

    private UserSubscriptionServiceImpl userSubscriptionService;

    @BeforeEach
    void setUp(){
        userSubscriptionService = new UserSubscriptionServiceImpl(
                userSubscriptionRepository,
                analyticSubscriptionService,
                userSubscriptionMapper,
                webClientService,
                cacheService,
                10);
    }

    @Test
    void saveSubscription_GroupFoundAndRequestCorrect_CorrectSaveAndCheckRepo(){
        RequestSubscriptionDto requestSubscriptionDto = new RequestSubscriptionDto(
                "ServiceName",
                "name",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(10),
                PaymentPeriod.MONTHLY,
                BigDecimal.valueOf(1000)
        );
        ResponseSubscriptionDto responseSubscriptionDto = new ResponseSubscriptionDto(
                1L,
                requestSubscriptionDto.serviceName(),
                requestSubscriptionDto.subscriptionName(),
                requestSubscriptionDto.startDate(),
                requestSubscriptionDto.endDate(),
                requestSubscriptionDto.paymentPeriod().toString(),
                requestSubscriptionDto.amount(),
                SubscriptionStatus.ACTIVE.toString(),
                OffsetDateTime.now());
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(analyticSubscriptionService.generateAnalyticSubscription(any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.save(any())).thenReturn(Mono.just(new UserSubscription()));
        when(userSubscriptionMapper.requestSubscriptionDtoToUserSubscription(any(), anyLong())).thenReturn(new UserSubscription());
        when(userSubscriptionMapper.userSubscriptionToResponseSubscriptionDto(any())).thenReturn(responseSubscriptionDto);
        when(cacheService.deleteValue(any())).thenReturn(Mono.empty());

        Mono<ResponseSubscriptionDto> result = userSubscriptionService.saveSubscription(1L, Mono.just(requestSubscriptionDto), null);

        StepVerifier.create(result)
                .expectNext(responseSubscriptionDto)
                .verifyComplete();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(analyticSubscriptionService).generateAnalyticSubscription(any());
        verify(userSubscriptionMapper).userSubscriptionToResponseSubscriptionDto(any());
        verify(cacheService).deleteValue(any());
    }

    @Test
    void saveSubscription_GroupFoundAndRequestNotCorrectStartDate_NotCorrectSaveAndCheckRepo(){
        RequestSubscriptionDto requestSubscriptionDto = new RequestSubscriptionDto(
                "ServiceName",
                "name",
                LocalDate.now().minusYears(100),
                LocalDate.now().plusMonths(10),
                PaymentPeriod.MONTHLY,
                BigDecimal.valueOf(1000)
        );

        Mono<ResponseSubscriptionDto> result = userSubscriptionService.saveSubscription(1L, Mono.just(requestSubscriptionDto), null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(webClientService, times(0)).checkUserIsOwnerGroup(anyLong(), any());
        verify(analyticSubscriptionService, times(0)).generateAnalyticSubscription(any());
        verify(userSubscriptionMapper, times(0)).userSubscriptionToResponseSubscriptionDto(any());
        verify(cacheService, times(0)).deleteValue(any());
    }
    @Test
    void saveSubscription_GroupFoundAndRequestNotCorrectEndTime_NotCorrectSaveAndCheckRepo(){
        RequestSubscriptionDto requestSubscriptionDto = new RequestSubscriptionDto(
                "ServiceName",
                "name",
                LocalDate.now(),
                LocalDate.now().minusMonths(1),
                PaymentPeriod.MONTHLY,
                BigDecimal.valueOf(1000)
        );

        Mono<ResponseSubscriptionDto> result = userSubscriptionService.saveSubscription(1L, Mono.just(requestSubscriptionDto), null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(webClientService, times(0)).checkUserIsOwnerGroup(anyLong(), any());
        verify(analyticSubscriptionService, times(0)).generateAnalyticSubscription(any());
        verify(userSubscriptionMapper, times(0)).userSubscriptionToResponseSubscriptionDto(any());
        verify(cacheService, times(0)).deleteValue(any());
    }

    @Test
    void updateSubscription_GroupFoundAndRequestCorrect_CorrectUpdateAndCheckRepo() {
        Map<SqlIdentifier, Object> map = new HashMap<>();
        map.put(SqlIdentifier.quoted("name"), "serviceName");
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionMapper.addUpdateField(any())).thenReturn(map);
        when(userSubscriptionRepository.updateFieldsByField(any(), any(), anyString(), any())).thenReturn(Mono.just(1L));

        Mono<Void> result = userSubscriptionService.updateSubscription(1L, 1L, Mono.just(new RequestUpdateSubscriptionDto(null, null, null, null)), null);

        StepVerifier.create(result)
                .verifyComplete();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).updateFieldsByField(any(), any(), anyString(), any());
        verify(userSubscriptionMapper).addUpdateField(any());
    }
    @Test
    void updateSubscription_GroupFoundAndRequestCorrectNotField_CorrectUpdateAndCheckRepo() {
        when(userSubscriptionMapper.addUpdateField(any())).thenReturn(Map.of());

        Mono<Void> result = userSubscriptionService.updateSubscription(1L, 1L, Mono.just(new RequestUpdateSubscriptionDto(null, null, null, null)), null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(webClientService, times(0)).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository, times(0)).updateFieldsByField(any(), any(), anyString(), any());
        verify(userSubscriptionMapper).addUpdateField(any());
    }
    @Test
    void getSubscriptionsGroup_GroupFound_CorrectReturnAndCheckRepo(){
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.findByGroupId(anyLong(), any())).thenReturn(Flux.empty());

        Mono<ResponseSubscriptionsDto> result = userSubscriptionService.getSubscriptionsGroup(1L, 1, null);

        StepVerifier.create(result)
                .assertNext(dto -> assertEquals(0, dto.subscriptions().size()))
                .verifyComplete();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(userSubscriptionRepository).findByGroupId(anyLong(), any());
    }
    @Test
    void getSubscriptionById_GroupFoundAndSubscriptionFound_CorrectReturnAndCheckRepo(){
        ResponseSubscriptionDto responseSubscriptionDto = new ResponseSubscriptionDto(
                1L,
                "ServiceName",
                "name",
                LocalDate.now().minusYears(100),
                LocalDate.now().plusMonths(10),
                PaymentPeriod.MONTHLY.toString(),
                BigDecimal.valueOf(1000),
                SubscriptionStatus.ACTIVE.toString(),
                OffsetDateTime.now()
        );
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.findBySubscriptionId(anyLong())).thenReturn(Mono.just(responseSubscriptionDto));

        Mono<ResponseSubscriptionDto> result = userSubscriptionService.getSubscriptionById(1L, 1L, null);

        StepVerifier.create(result)
                .expectNext(responseSubscriptionDto)
                .verifyComplete();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(userSubscriptionRepository).findBySubscriptionId(anyLong());
    }
    @Test
    void getSubscriptionById_GroupFoundAndSubscriptionNotFound_CorrectReturnAndCheckRepo(){
        when(webClientService.checkUserInGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.findBySubscriptionId(anyLong())).thenReturn(Mono.empty());

        Mono<ResponseSubscriptionDto> result = userSubscriptionService.getSubscriptionById(1L, 1L, null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserInGroup(anyLong(), any());
        verify(userSubscriptionRepository).findBySubscriptionId(anyLong());
    }
    @Test
    void deleteSubscriptionById_GroupFoundAndSubscriptionFound_CorrectReturnAndCheckRepo(){
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.deleteByIdReturningCount(anyLong())).thenReturn(Mono.just(1));

        Mono<Void> result = userSubscriptionService.deleteSubscriptionById(1L, 1L, null);

        StepVerifier.create(result)
                        .verifyComplete();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).deleteByIdReturningCount(anyLong());
    }
    @Test
    void deleteSubscriptionById_GroupFoundAndSubscriptionNotFound_CorrectDeleteAndCheckRepo(){
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.deleteByIdReturningCount(anyLong())).thenReturn(Mono.just(0));

        Mono<Void> result = userSubscriptionService.deleteSubscriptionById(1L, 1L, null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).deleteByIdReturningCount(anyLong());
    }
    @Test
    void deleteSubscriptionsByGroupId_GroupFound_CorrectDeleteAndCheckRepo(){
        when(userSubscriptionRepository.deleteByGroupId(anyLong())).thenReturn(Mono.just(1));

        Mono<Void> result = userSubscriptionService.deleteSubscriptionsByGroupId(anyLong());

        StepVerifier.create(result)
                .verifyComplete();
        verify(userSubscriptionRepository).deleteByGroupId(anyLong());
    }
    @Test
    void deleteSubscriptionsByGroupId_GroupFoundAndNotSubscriptions_CorrectDeleteAndCheckRepo(){
        when(userSubscriptionRepository.deleteByGroupId(anyLong())).thenReturn(Mono.just(0));

        Mono<Void> result = userSubscriptionService.deleteSubscriptionsByGroupId(1L);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()));
        verify(userSubscriptionRepository).deleteByGroupId(anyLong());
    }
    @Test
    void renewSubscriptionById_GroupFoundAndSubscriptionFoundAndSubscriptionNotEXPIRED_CorrectUpdateAndCheckRepo(){
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.updateEndTimeSubscriptionById(anyLong(), anyLong())).thenReturn(Mono.just(1));


        Mono<Void> result = userSubscriptionService.renewSubscriptionById(1L, 1L, 1L, null);

        StepVerifier.create(result)
                .verifyComplete();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).updateEndTimeSubscriptionById(anyLong(), anyLong());
        verify(userSubscriptionRepository, times(0)).existsByExpired(anyLong());
    }
    @Test
    void renewSubscriptionById_GroupFoundAndSubscriptionNotFound_CorrectUpdateAndCheckRepo(){
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.updateEndTimeSubscriptionById(anyLong(), anyLong())).thenReturn(Mono.just(0));
        when(userSubscriptionRepository.existsByExpired(anyLong())).thenReturn(Mono.just(Boolean.FALSE));

        Mono<Void> result = userSubscriptionService.renewSubscriptionById(1L, 1L, 1L, null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(NoSuchElementException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).updateEndTimeSubscriptionById(anyLong(), anyLong());
        verify(userSubscriptionRepository).existsByExpired(anyLong());
    }

    @Test
    void renewSubscriptionById_GroupFoundAndSubscriptionEXPIRED_CorrectUpdateAndCheckRepo(){
        when(webClientService.checkUserIsOwnerGroup(anyLong(), any())).thenReturn(Mono.empty());
        when(userSubscriptionRepository.updateEndTimeSubscriptionById(anyLong(), anyLong())).thenReturn(Mono.just(0));
        when(userSubscriptionRepository.existsByExpired(anyLong())).thenReturn(Mono.just(Boolean.TRUE));

        Mono<Void> result = userSubscriptionService.renewSubscriptionById(1L, 1L, 1L, null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(webClientService).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository).updateEndTimeSubscriptionById(anyLong(), anyLong());
        verify(userSubscriptionRepository).existsByExpired(anyLong());
    }
    @Test
    void renewSubscriptionById_GroupFoundAndExtensionCountNotCorrect_NotCorrectUpdateAndCheckRepo(){

        Mono<Void> result = userSubscriptionService.renewSubscriptionById(1L, 1L, -1L, null);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> assertEquals(ValidationException.class, throwable.getClass()))
                .verify();
        verify(webClientService, times(0)).checkUserIsOwnerGroup(anyLong(), any());
        verify(userSubscriptionRepository, times(0)).updateEndTimeSubscriptionById(anyLong(), anyLong());
        verify(userSubscriptionRepository, times(0)).existsByExpired(anyLong());
    }


}
