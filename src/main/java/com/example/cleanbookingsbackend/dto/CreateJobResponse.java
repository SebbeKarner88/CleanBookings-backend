package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.JobType;
import lombok.Builder;


@Builder
public record CreateJobResponse(String jobId, String html_snippet) {
}
