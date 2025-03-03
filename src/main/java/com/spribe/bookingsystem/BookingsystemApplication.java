package com.spribe.bookingsystem;

import com.spribe.bookingsystem.config.PaymentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(PaymentProperties.class)
public class BookingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingsystemApplication.class, args);
	}

}
