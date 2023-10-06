package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.Role;

public record EmployeeAuthenticationResponse(String employeeId, String username, Role role) {
}
