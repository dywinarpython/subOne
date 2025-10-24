package com.subOne.notifications_service.mapper;

import com.subOne.notifications_service.entity.Notification;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperNotification {

    @Mappings(value = {
            @Mapping(target = "message", expression = "java(record.value())"),
            @Mapping(target = "userId", expression = "java(record.key())"),
            @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    })
    Notification messageDtoToNotification(ConsumerRecord<UUID, String> record);

    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    Notification parametersToNotification(String message, UUID userId);


}
