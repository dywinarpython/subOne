package com.subOne.notifications_service.kafka;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Profile("!test")
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final Environment env;

    private void generateRetry(ConcurrentKafkaListenerContainerFactory<?, ?> factory){
        FixedBackOff backOff = new FixedBackOff(5000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        factory.setCommonErrorHandler(errorHandler);
    }

    @Bean
    public ConsumerFactory<String, UserInfo> userInfoConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.bootstrap-servers"));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG,
                env.getProperty("spring.kafka.consumer.group-id"));
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES, "com.subOne.keycloak_dto");
        cfg.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.subOne.keycloak_dto.UserInfo");
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConsumerFactory<UUID, KafkaDtoPaymentSubscription> paymentConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.bootstrap-servers"));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG,
                env.getProperty("spring.kafka.consumer.group-id"));
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES, "com.subOne.kafka_dto");
        cfg.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.subOne.kafka_dto.KafkaDtoPaymentSubscription");
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConsumerFactory<UUID, SendNotificationDto> notificationConsumerFactory() {
        Map<String, Object> cfg = new HashMap<>();
        cfg.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.bootstrap-servers"));
        cfg.put(ConsumerConfig.GROUP_ID_CONFIG,
                env.getProperty("spring.kafka.consumer.group-id"));
        cfg.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        cfg.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        cfg.put(JsonDeserializer.TRUSTED_PACKAGES, "com.subOne.kafka_dto");
        cfg.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.subOne.kafka_dto.SendNotificationDto");
        return new DefaultKafkaConsumerFactory<>(cfg);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, KafkaDtoPaymentSubscription> paymentKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, KafkaDtoPaymentSubscription> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory());
        factory.setConcurrency(3);
        generateRetry(factory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserInfo> userInfoKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserInfo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userInfoConsumerFactory());
        factory.setConcurrency(3);
        generateRetry(factory);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, SendNotificationDto> notificationKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, SendNotificationDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        factory.setConcurrency(3);
        generateRetry(factory);
        return factory;
    }
}
