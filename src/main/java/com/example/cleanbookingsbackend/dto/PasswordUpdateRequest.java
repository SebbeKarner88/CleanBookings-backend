package com.example.cleanbookingsbackend.dto;

import lombok.Data;


public record PasswordUpdateRequest(String oldPassword, String newPassword) {
}
