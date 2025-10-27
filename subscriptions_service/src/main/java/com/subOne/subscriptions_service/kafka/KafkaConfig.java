package com.subOne.subscriptions_service.kafka;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("!test")
public class KafkaConfig {

    private NewTopic createTopic(String name, int partitions){
        return TopicBuilder.name(name).partitions(partitions).build();
    }

    @Bean
    public NewTopic paymentSubscription() { return createTopic("payment_subscription", 3);}

    @Bean
    public ProducerFactory<String, KafkaDtoPaymentSubscription> kafkaDtoPaymentSubscriptionProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @Qualifier("messageSendWithKafkaDtoPaymentSubscription")
    public KafkaTemplate<String, KafkaDtoPaymentSubscription> messageSendWithKafkaDtoPaymentSubscription(ProducerFactory<String, KafkaDtoPaymentSubscription> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }



}
