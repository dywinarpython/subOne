package com.subOne.user_service.config;

import com.subOne.user_service.kafka.serviceProducer.KafkaService;
import com.subOne.user_service.kafka.serviceProducer.KafkaServiceImpl;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;


@TestConfiguration
public class TestConfig {

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(@Qualifier("redisContainer") GenericContainer<?> redisContainer) {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(redisContainer.getHost(), redisContainer.getFirstMappedPort());
        return new LettuceConnectionFactory(serverConfig);
    }

    @Bean
    @Primary
    public ReactiveTransactionManager connectionFactoryTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }


    @Bean public DataSource dataSource(PostgreSQLContainer<?> postgreSQLContainer){
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(postgreSQLContainer.getJdbcUrl());
        hikariDataSource.setUsername(postgreSQLContainer.getUsername());
        hikariDataSource.setPassword(postgreSQLContainer.getPassword());
        return hikariDataSource;
    }

    @Bean public ConnectionFactory connectionFactory(PostgreSQLContainer<?> postgreSQLContainer) {
        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(postgreSQLContainer.getHost())
                .port(postgreSQLContainer.getFirstMappedPort())
                .database(postgreSQLContainer.getDatabaseName())
                .username(postgreSQLContainer.getUsername())
                .password(postgreSQLContainer.getPassword())
                .build();
        return new ConnectionPool(ConnectionPoolConfiguration
                .builder(new PostgresqlConnectionFactory(config))
                .build());
    }

    @Primary
    @Bean
    public KafkaService kafkaService(){
        KafkaService kafkaService = mock(KafkaServiceImpl.class);
        lenient().when(kafkaService.sendToTopic(anyString(), anyString())).thenReturn(Mono.empty());
        lenient().when(kafkaService.sendToTopic(anyString(), anyLong())).thenReturn(Mono.empty());
        return kafkaService;
    }

}
