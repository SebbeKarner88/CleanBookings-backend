package com.example.cleanbookingsbackend.dto;

import java.util.List;

public record AssignCleanerRequest(String jobId, String adminId, List<String> cleanerId) {
}
