package com.example.cleanbookingsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173/", "http://localhost:3000/")
                .allowedMethods("*")
                .allowedHeaders("*")
//                .allowCredentials(true)
                .maxAge(3600);
    }
}