package com.spribe.bookingsystem;

import org.springframework.boot.SpringApplication;

public class TestBookingsystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookingsystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
