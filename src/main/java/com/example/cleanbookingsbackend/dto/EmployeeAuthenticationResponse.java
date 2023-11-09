package com.example.cleanbookingsbackend.dto;

public record EmployeeAuthenticationResponse(String employeeId, String username, String role, String accessToken, String refreshToken) {
}
