package com.subOne.user_service.service_impl;

import com.subOne.keycloak_dto.UserInfo;
import com.subOne.user_service.cache.CacheService;
import com.subOne.user_service.dto.user.request.RequestUpdateUserDto;
import com.subOne.user_service.entity.User;
import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.mapper.MapperUser;
import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.repository.user_repository.UserRepository;
import com.subOne.user_service.service.GroupMemberService;
import com.subOne.user_service.service.GroupService;
import com.subOne.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    private final KafkaService kafkaService;
    private final UserRepository userRepository;
    private final MapperUser mapperUser;
    private final String nameTopicDeleteUser;
    private final CacheService cacheService;
    private final GroupMemberService groupMemberService;
    private final GroupService groupService;

    public UserServiceImpl(KafkaService kafkaService, UserRepository userRepository, MapperUser mapperUser, @Value("${spring.kafka.topicNameDeleteUser:delete_user}") String nameTopicDeleteUser, CacheService cacheService, GroupMemberService groupMemberService, GroupService groupService) {
        this.kafkaService = kafkaService;
        this.userRepository = userRepository;
        this.mapperUser = mapperUser;
        this.nameTopicDeleteUser = nameTopicDeleteUser;
        this.cacheService = cacheService;
        this.groupMemberService = groupMemberService;
        this.groupService = groupService;
    }


    @Override
    @Transactional
    public Mono<Void> saveUser(UserInfo userInfo) {
        return userRepository.save(mapperUser.userInfoToUser(userInfo)).then();
    }

    @Override
    @Transactional
    public Mono<Void> addVerifyEmailUser(String email) {
        return userRepository.updateToVerifyEmail(email).flatMap(count -> {
            if(count != 1) return Mono.error(new NoSuchElementException("Email is not update!"));
            return Mono.empty();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<ResponseUserDto> getUserById(Jwt jwt) {
        return cacheService.getValue("USER::" + jwt.getSubject(), ResponseUserDto.class)
                .switchIfEmpty( Mono.defer( ()  ->
                        userRepository.findByUserId(UUID.fromString(jwt.getSubject()))
                        .switchIfEmpty(Mono.error(new NoSuchElementException("User is not found!")))
                        .flatMap(dto -> cacheService.saveValue("USER::" + jwt.getSubject(), dto, Duration.ofMinutes(15)).thenReturn(dto)))
                );
    }

    @Override
    @Transactional
    public Mono<Void> updateUser(Mono<RequestUpdateUserDto> requestUpdateUserDto, Jwt jwt) {
        return requestUpdateUserDto.map(mapperUser::addUpdateField).flatMap(mp ->
                userRepository.updateFields(mp, User.class, "userId", jwt.getSubject())).flatMap( count -> {
                    if(count != 1) return Mono.error(new NoSuchElementException("User is not found!"));
                    return cacheService.deleteValue("USER::" + jwt.getSubject());
                });
    }

    @Override
    @Transactional
    public Mono<Void> deleteUser(Jwt jwt) {
        return cacheService.getValue("USER::" + jwt.getSubject(), ResponseUserDto.class)
                .map(responseUserDto -> responseUserDto.userId().equals(UUID.fromString(jwt.getSubject())))
                .switchIfEmpty(Mono.defer(() -> userRepository.existsByUserId(UUID.fromString(jwt.getSubject()))))
                .flatMap(bl -> bl? Mono.empty(): Mono.error(new NoSuchElementException("User is not found")))
                .then(Mono.defer( () -> groupMemberService.existsMemberInGroupByOwnerId(jwt)))
                .then(Mono.defer( () -> groupService.deleteDataRelatedGroupsByOwnerId(jwt)))
                .then(Mono.defer( () -> userRepository.deleteByUserId(UUID.fromString(jwt.getSubject()))
                .then(Mono.defer( () -> kafkaService.sendToTopic(nameTopicDeleteUser, jwt.getSubject())))
                .then(Mono.defer( () -> cacheService.deleteValue("USER::" + jwt.getSubject())))));
    }

    @Override
    public Mono<Boolean> checkVerifyEmail(UUID userId) {
        return userRepository.existsByUserIdAndVerifyEmailTrue(userId);
    }
}
