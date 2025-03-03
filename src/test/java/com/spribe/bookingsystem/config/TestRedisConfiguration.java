package com.spribe.bookingsystem.config;

import static org.springframework.test.context.support.TestPropertySourceUtils.addInlinedPropertiesToEnvironment;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@RequiredArgsConstructor
public class TestRedisConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:latest"))
        .withExposedPorts(6379)
        .withCommand("redis-server", "--appendonly", "yes", "--notify-keyspace-events", "Ex");

    @Override
    @SneakyThrows
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        REDIS_CONTAINER.start();

        String redisHost = REDIS_CONTAINER.getHost();
        Integer redisPort = REDIS_CONTAINER.getMappedPort(6379);

        addInlinedPropertiesToEnvironment(applicationContext,
            "spring.data.redis.host=" + redisHost,
            "spring.data.redis.port=" + redisPort,
            "spring.redis.notify-keyspace-events=Ex"
        );
    }

    @PreDestroy
    void cleanUp() {
        REDIS_CONTAINER.stop();
    }

    @Bean
    public GenericContainer<?> redisContainer() {
        return REDIS_CONTAINER;
    }
}
