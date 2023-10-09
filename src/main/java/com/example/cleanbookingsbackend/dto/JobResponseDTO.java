package com.example.cleanbookingsbackend.dto;

import java.util.List;

public record JobResponseDTO(String jobId, String jobType, String jobStatus, String jobMessage, String customerId, List<String> employees) {
}
