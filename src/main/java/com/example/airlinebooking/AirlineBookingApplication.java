package com.example.airlinebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Spring Boot entrypoint to keep bootstrapping minimal for the exercise.
 */
@SpringBootApplication
@EnableJdbcRepositories
public class AirlineBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(AirlineBookingApplication.class, args);
    }
}
