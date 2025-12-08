package com.subOne.user_service.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    private Integer id;
    private UUID userId;
    private String name;
    private String surname;
    private String email;
    private Boolean verifyEmail;
}
