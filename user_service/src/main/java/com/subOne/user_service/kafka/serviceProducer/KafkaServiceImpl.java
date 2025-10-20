package com.subOne.user_service.kafka.serviceProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    @Qualifier("messageSendWithString")
    private KafkaTemplate<String, String> kafkaTemplateString;

    @Autowired
    @Qualifier("messageSendWithLong")
    private KafkaTemplate<String, Long> kafkaTemplateLong;

    @Override
    public Mono<Void> sendToTopic(String nameTopic, String value) {
        return Mono.fromFuture(kafkaTemplateString.send(nameTopic, value)).then();
    }

    @Override
    public Mono<Void> sendToTopic(String nameTopic, Long value) {
        return Mono.fromFuture(kafkaTemplateLong.send(nameTopic, value)).then();
    }
}
