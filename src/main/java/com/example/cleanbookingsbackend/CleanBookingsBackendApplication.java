package com.example.cleanbookingsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@EnableScheduling
public class CleanBookingsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleanBookingsBackendApplication.class, args);
    }


}
