package com.example.cleanbookingsbackend.dto;

public record UserUpdateRequest(
        String customerId,
        String firstName,
        String lastName,
        String streetAddress,
        Integer postalCode,
        String city,
        String phoneNumber,
        String emailAddress
) {}
