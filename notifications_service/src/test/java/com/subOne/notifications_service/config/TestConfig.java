package com.subOne.notifications_service.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;


@TestConfiguration
public class TestConfig {
    @Bean
    public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariDataSource.setUsername(postgreSQLContainer.getUsername());
        hikariDataSource.setPassword(postgreSQLContainer.getPassword());
        return hikariDataSource;
    }
    @Bean
    public JwtDecoder jwtDecoder(){
        return mock(JwtDecoder.class);
    }
}
