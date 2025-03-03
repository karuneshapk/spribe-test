package com.spribe.bookingsystem.config;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
    @DefaultValue("15") long expirationTime,
    @DefaultValue("MINUTES") TimeUnit timeUnit
) {
}

