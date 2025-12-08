package com.subOne.user_service.kafka;

import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.keycloak_dto.UserInfo;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@Profile("!test")
public class KafkaConfig {

    private final Environment env;
    private final int partitions;
    private final String bootstrapServers;
    private final boolean enableIdempotence;
    private final int requestTimeout;
    private final int deliveryTimeout;

    public KafkaConfig(
            Environment env, @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.properties.enable.idempotence}") boolean enableIdempotence,
            @Value("${spring.kafka.properties.request.timeout.ms}") int requestTimeout,
            @Value("${spring.kafka.properties.delivery.timeout.ms}") int deliveryTimeout,
            @Value("${spring.kafka.partitions_keycloak_consumer}") int partitions) {
        this.env = env;
        this.bootstrapServers = bootstrapServers;
        this.enableIdempotence = enableIdempotence;
        this.requestTimeout = requestTimeout;
        this.deliveryTimeout = deliveryTimeout;
        this.partitions = partitions;
    }

    private Map<String, Object> generateDefaultProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeout);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeout);
        return props;
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
    public ConcurrentKafkaListenerContainerFactory<String, UserInfo> userInfoKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserInfo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userInfoConsumerFactory());
        factory.setConcurrency(3);
        FixedBackOff backOff = new FixedBackOff(5000L, 3L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(backOff);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    private NewTopic createTopic(String name){
        return TopicBuilder.name(name).partitions(partitions).build();
    }

    private NewTopic createTopic(String name, int partitions){
        return TopicBuilder.name(name).partitions(partitions).build();
    }

    @Bean
    public NewTopic messageUserTopic(){
        return createTopic("delete_user", 1);
    }

    @Bean
    public NewTopic messageDeleteGroup() { return createTopic("delete_group");}

    @Bean
    public NewTopic notifications() { return createTopic("notification_user");}


    @Bean
    public ProducerFactory<String, String> groupStringProducerFactory() {
        Map<String, Object> props = generateDefaultProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, Integer> groupLongProducerFactory() {
        Map<String, Object> props = generateDefaultProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<UUID, SendNotificationDto> notificationsUUIDProducerFactory() {
        Map<String, Object> props = generateDefaultProps();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @Qualifier("messageSendWithUUIDKeyNotification")
    public KafkaTemplate<UUID, SendNotificationDto> messageSendWithUUIDKey(ProducerFactory<UUID, SendNotificationDto> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("messageSendWithInteger")
    public KafkaTemplate<String, Integer> messageSendWithInteger(ProducerFactory<String, Integer> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("messageSendWithString")
    public KafkaTemplate<String, String> messageSendWithString(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

}
