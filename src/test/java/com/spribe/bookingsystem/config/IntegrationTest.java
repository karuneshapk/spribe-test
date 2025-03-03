package com.spribe.bookingsystem.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.TimeUnit.MINUTES;
import com.spribe.bookingsystem.BookingsystemApplication;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest(classes = BookingsystemApplication.class)
@ActiveProfiles(value = "integration-test")
@ContextConfiguration(initializers = {TestRedisConfiguration.class, TestPostgresqlConfiguration.class})
@AutoConfigureMockMvc
@Timeout(value = 1, unit = MINUTES)
public @interface IntegrationTest {
}
