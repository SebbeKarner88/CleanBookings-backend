package com.example.cleanbookingsbackend.dto;

public record PasswordUpdateRequest(String oldPassword, String newPassword) {
}
