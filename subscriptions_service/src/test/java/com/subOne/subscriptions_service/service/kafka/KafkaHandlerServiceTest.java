package com.subOne.subscriptions_service.service.kafka;

import com.subOne.subscriptions_service.kafka.kasfka_handler_impl.KafkaHandlerServiceImpl;
import com.subOne.subscriptions_service.service.UserSubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaHandlerServiceTest {

    @Mock
    private UserSubscriptionService userSubscriptionService;

    @InjectMocks
    private KafkaHandlerServiceImpl kafkaHandlerService;

    @Test
    void deleteSubscriptionsByGroup_SubscriptionsFound_CorrectDeleteAndCheck(){
        when(userSubscriptionService.deleteSubscriptionsByGroupId(anyInt())).thenReturn(Mono.empty());

        kafkaHandlerService.deleteSubscriptionsByGroup(1);

        verify(userSubscriptionService).deleteSubscriptionsByGroupId(anyInt());

    }

}
