package com.subOne.user_service.kafka;

import com.subOne.keycloak_dto.UserInfo;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

// TODO настроить параллельность при обработки в consumer (Consumer)
@Configuration
@Profile("!test")
public class KafkaConfig {

    @Value("${spring.kafka.partitions_keycloak_consumer}")
    private int partitions;

    @Autowired
    private Environment env;

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
        return createTopic("delete_user");
    }

    @Bean
    public NewTopic messageDeleteGroup() { return createTopic("delete_group", 3);}

    @Bean
    public ProducerFactory<String, String> groupStringProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, Long> groupLongProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }


    @Bean
    @Qualifier("messageSendWithLong")
    public KafkaTemplate<String, Long> messageSendWithLong(ProducerFactory<String, Long> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @Qualifier("messageSendWithString")
    public KafkaTemplate<String, String> messageSendWithString(
            ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }





}
