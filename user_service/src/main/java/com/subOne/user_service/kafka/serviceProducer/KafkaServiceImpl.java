package com.subOne.user_service.kafka.serviceProducer;

import com.subOne.kafka_dto.SendNotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    @Qualifier("messageSendWithString")
    private KafkaTemplate<String, String> kafkaTemplateString;

    @Autowired
    @Qualifier("messageSendWithInteger")
    private KafkaTemplate<String, Integer> kafkaTemplateInteger;

    @Autowired
    @Qualifier("messageSendWithUUIDKeyNotification")
    private KafkaTemplate<UUID, SendNotificationDto> kafkaDtoSendNotificationKafkaTemplate;

    @Override
    public Mono<Void> sendToTopic(String nameTopic, String value) {
        return Mono.fromFuture(kafkaTemplateString.send(nameTopic, value)).then();
    }

    @Override
    public Mono<Void> sendToTopic(String nameTopic, Integer value) {
        return Mono.fromFuture(kafkaTemplateInteger.send(nameTopic, value)).then();
    }

    @Override
    public Mono<Void> sendToTopic(String nameTopic, UUID userId, SendNotificationDto kafkaDtoSendNotification) {
        return Mono.fromFuture(kafkaDtoSendNotificationKafkaTemplate.send(nameTopic, userId, kafkaDtoSendNotification)).then();
    }

}
