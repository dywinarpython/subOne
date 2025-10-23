package com.subOne.user_service.repository.user_repository;

import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.repository.user_repository.update.UpdateRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, Long>, UpdateRepository {

    Mono<ResponseUserDto> findByUserId(UUID id);
    Mono<Integer> deleteByUserId(UUID id);
    Mono<Boolean> existsByUserId(UUID userId);
    Mono<Boolean> existsByUserIdAndVerifyEmailTrue(UUID userId);

    @Modifying
    @Query("""
    update users
    set verify_email = true
    where email = :email
    """)
    Mono<Integer> updateToVerifyEmail(@Param("email") String email);

}
