package com.subOne.user_service.mapper;

import com.subOne.kecyloak_dto.UserInfo;
import com.subOne.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MapperUser {

    @Mapping(source = "email_verify", target = "verifyEmail")
    User userInfoToUser(UserInfo userInfo);
}
