package com.example.cleanbookingsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://localhost:5173",
                        "https://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "https://www.localhost:5173",
                        "https://www.localhost:3000",
                        "http://www.localhost:5173",
                        "http://www.localhost:3000"
                )
                .allowedMethods("*")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
