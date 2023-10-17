package com.example.cleanbookingsbackend.dto;

public record AdminEmployeeUpdateRequest(
        String adminId,
        String employeeId,
        String firstName,
        String lastName,
        String emailAddress,
        String phoneNumber
        ){}
