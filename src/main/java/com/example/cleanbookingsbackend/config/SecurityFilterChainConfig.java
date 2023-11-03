package com.example.cleanbookingsbackend.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {
    private final JwtAuthConverter jwtAuthConverter;
    private final CorsConfigurationSource corsConfiguration;
    @Value("${KC_REALM}")
    private String realm;

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwk = "http://localhost:8080/realms/" + realm + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwk).build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(config -> config.configurationSource(corsConfiguration))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req
                                .requestMatchers(HttpMethod.POST, "/api/v1/customer/login", "/api/v1/customer", "api/v1/customer/refresh-token", "/api/v1/employee/refresh-token", "/api/v1/customer/logout", "/api/v1/employee/logout").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/employee/getAllCleaners").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/v1/employee/login").permitAll()
                                .requestMatchers("/api/v1/admin/**", "/api/v1/payment/**").hasRole("client_admin")
                                .anyRequest()
                                .authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .build();
    }
}
