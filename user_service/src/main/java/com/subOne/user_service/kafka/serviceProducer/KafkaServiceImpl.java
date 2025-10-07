package com.subOne.user_service.kafka.serviceProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KafkaServiceImpl implements KafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplateString;

    @Override
    public Mono<Void> sendToTopic(String nameTopic, String value) {
        return Mono.fromFuture(kafkaTemplateString.send(nameTopic, value)).then();
    }
}
