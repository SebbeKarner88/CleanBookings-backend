package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.Role;

public record CreateEmployeeRequest(String firstName, String lastName, String phoneNumber, Role role, String emailAddress, String password ) {
}
