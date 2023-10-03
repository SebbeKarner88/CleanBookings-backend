package com.example.cleanbookingsbackend.dto;

public record JobApproveRequest(String jobId, String customerId, boolean isApproved) {
}
