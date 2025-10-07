package com.subOne.user_service.config;

import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;


@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public CacheManager cacheManager(){
        return new NoOpCacheManager();
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

}
