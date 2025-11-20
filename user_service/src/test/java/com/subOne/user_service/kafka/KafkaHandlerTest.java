package com.subOne.user_service.kafka;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.user_service.BaseIntegrationTest;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.kasfka_handler.KafkaHandlerService;
import com.subOne.user_service.kafka.serviceProducer.KafkaServiceImpl;
import com.subOne.user_service.repository.user_repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KafkaHandlerTest extends BaseIntegrationTest {

    @Autowired
    private KafkaHandlerService kafkaHandlerService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KafkaServiceImpl kafkaService;


    @Test
    void saveUserForKeycloak_CorrectSave(){
        UserInfo userInfo = new UserInfo(
                "name", "surname", UUID.randomUUID(), "testEmail" + System.currentTimeMillis() +  "@mail.com", false
        );
        kafkaHandlerService.saveUserForKeycloak(userInfo);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> StepVerifier.create(userRepository.findByUserId(userInfo.userId()))
                .assertNext(dto -> {
                  assertEquals(dto.email(), userInfo.email());
                  assertEquals(dto.name(), userInfo.name());
                  assertEquals(dto.surname(), userInfo.surname());
                }).verifyComplete());
    }

    @Test
    void addVerifyEmailUser_CorrectSave(){
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("testEmail" + System.currentTimeMillis() +  "@mail.com");
        user.setName("test");
        user.setSurname("testSurname");
        user.setVerifyEmail(false);
        user = userRepository.save(user).block();
        assertNotNull(user);

        kafkaHandlerService.addVerifyEmailUser(user.getEmail());

        User finalUser = user;
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> StepVerifier.create(userRepository.existsByUserIdAndVerifyEmailTrue(finalUser.getUserId()))
                .expectNext(true)
                .verifyComplete());
    }

}
