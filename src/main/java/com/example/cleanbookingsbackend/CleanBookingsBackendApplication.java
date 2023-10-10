package com.example.cleanbookingsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CleanBookingsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanBookingsBackendApplication.class, args);
    }

}
