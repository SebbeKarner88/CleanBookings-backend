package com.example.cleanbookingsbackend.dto;

public record AuthenticationResponse(String customerId, String name, String accessToken, String refreshToken, String role) {
}
