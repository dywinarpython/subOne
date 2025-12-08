package com.subOne.subscriptions_service.kafka.producer;

import com.subOne.kafka_dto.KafkaDtoPaymentSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
@Service
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    @Qualifier("messageSendWithKafkaDtoPaymentSubscription")
    private KafkaTemplate<String, KafkaDtoPaymentSubscription> kafkaTemplateDto;

    @Override
    public Mono<Void> sendToTopic(String nameTopic, KafkaDtoPaymentSubscription dto) {
        return Mono.fromFuture(kafkaTemplateDto.send(nameTopic, dto)).then();
    }
}
