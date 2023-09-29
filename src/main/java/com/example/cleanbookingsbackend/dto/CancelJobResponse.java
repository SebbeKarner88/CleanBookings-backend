package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.JobType;

public record CancelJobResponse(String jobId, JobType jobType, String date,  String message) {

}
