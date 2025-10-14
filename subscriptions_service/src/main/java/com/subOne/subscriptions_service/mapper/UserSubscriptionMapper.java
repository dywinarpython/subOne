package com.subOne.subscriptions_service.mapper;

import com.subOne.subscriptions_service.dto.request.RequestSubscriptionDto;
import com.subOne.subscriptions_service.dto.request.RequestUpdateSubscriptionDto;
import com.subOne.subscriptions_service.dto.response.ResponseSubscriptionDto;
import com.subOne.subscriptions_service.entity.UserSubscription;
import com.subOne.subscriptions_service.entity.enumEntity.SubscriptionStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import java.util.HashMap;
import java.util.Map;


@Mapper(componentModel = "spring")
public interface UserSubscriptionMapper {

    @Mappings(value = {
            @Mapping(target = "status", expression = "java(\"ACTIVE\")"),
            @Mapping(target = "paymentPeriod", expression = "java(requestSubscriptionDto.paymentPeriod().toString())")
    })
    UserSubscription requestSubscriptionDtoToUserSubscription(RequestSubscriptionDto requestSubscriptionDto, Long groupId);


    ResponseSubscriptionDto userSubscriptionToResponseSubscriptionDto(UserSubscription userSubscription);

    default Map<SqlIdentifier, Object> addUpdateField(RequestUpdateSubscriptionDto requestUpdateSubscriptionDto){

        Map<SqlIdentifier, Object> mp = new HashMap<>();
        if(requestUpdateSubscriptionDto.subscriptionName() != null){
            mp.put(SqlIdentifier.quoted("subscription_name"), requestUpdateSubscriptionDto.subscriptionName());
        }
        if (requestUpdateSubscriptionDto.serviceName() != null){
            mp.put(SqlIdentifier.quoted("service_name"), requestUpdateSubscriptionDto.serviceName());
        }
        if (requestUpdateSubscriptionDto.startDate() != null){
            mp.put(SqlIdentifier.quoted("start_date"), requestUpdateSubscriptionDto.startDate());
        }
        if (requestUpdateSubscriptionDto.endDate() != null){
            mp.put(SqlIdentifier.quoted("end_date"), requestUpdateSubscriptionDto.endDate());
        }
        if (requestUpdateSubscriptionDto.amount() != null){
            mp.put(SqlIdentifier.quoted("amount"), requestUpdateSubscriptionDto.amount());
        }
        if (requestUpdateSubscriptionDto.paymentPeriod() != null){
            mp.put(SqlIdentifier.quoted("payment_period"), requestUpdateSubscriptionDto.paymentPeriod().toString());
        }
        if (requestUpdateSubscriptionDto.statusStop() != null && requestUpdateSubscriptionDto.statusStop()){
            mp.put(SqlIdentifier.quoted("status"), SubscriptionStatus.STOP.toString());
        } else if(requestUpdateSubscriptionDto.statusStop() != null){
            mp.put(SqlIdentifier.quoted("status"), SubscriptionStatus.ACTIVE.toString());
        }
        return mp;
    }
}
