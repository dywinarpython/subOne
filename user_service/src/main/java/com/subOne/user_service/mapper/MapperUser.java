package com.subOne.user_service.mapper;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.entity.User;
import jakarta.validation.ValidationException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.relational.core.sql.SqlIdentifier;

import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface MapperUser {

    @Mapping(source = "email_verify", target = "verifyEmail")
    User userInfoToUser(UserInfo userInfo);

    default Map<SqlIdentifier, Object> addUpdateField(RequestUpdateUserDto requestUpdateUserDto){
        Map<SqlIdentifier, Object> mp = new HashMap<>();
        if(requestUpdateUserDto.name() != null){
            mp.put(SqlIdentifier.quoted("name"), requestUpdateUserDto.name());
        }
        if (requestUpdateUserDto.surname() != null){
            mp.put(SqlIdentifier.quoted("surname"), requestUpdateUserDto.surname());
        }
        if(requestUpdateUserDto.surname() == null && requestUpdateUserDto.name() == null){
            throw new ValidationException("Surname or name have not been transferred");
        }
        return mp;
    }
}
