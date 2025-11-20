package com.subOne.notifications_service.mapper;

import com.subOne.kafka_dto.SendNotificationDto;
import com.subOne.notification.NotificationTargetType;
import com.subOne.notification.NotificationType;
import com.subOne.notifications_service.entity.Notification;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface MapperNotification {




    @Mappings(value = {
            @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())"),
            @Mapping(target = "read",expression = "java(Boolean.FALSE)")
    })
    Notification parametersToNotification(UUID userId, NotificationType notificationType, NotificationTargetType notificationTargetType, Long targetId);


    default Notification messageDtoToNotification(ConsumerRecord<UUID, SendNotificationDto> record){
        Notification notification = new Notification();
        if(record != null){
            SendNotificationDto sendNotificationDto = record.value();
            notification.setNotificationTargetType(sendNotificationDto.notificationTargetType());
            notification.setCreatedAt(OffsetDateTime.now());
            notification.setNotificationType(sendNotificationDto.notificationType());
            notification.setUserId(record.key());
            notification.setRead(Boolean.FALSE);
            notification.setTargetId(sendNotificationDto.targetId());
        }
        return notification;
    }


}
