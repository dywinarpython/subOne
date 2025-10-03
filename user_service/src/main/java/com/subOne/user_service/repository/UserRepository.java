package com.subOne.user_service.repository;

import com.subOne.user_service.dto.response.UserResponseDto;
import com.subOne.user_service.entity.User;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    Mono<UserResponseDto> findUserById(UUID id);

    @Modifying
    @Query("""
    update users
    set verify_email = true
    where email = :email
    """)
    Mono<Integer> updateToVerifyEmail(@Param("email") String email);

}
