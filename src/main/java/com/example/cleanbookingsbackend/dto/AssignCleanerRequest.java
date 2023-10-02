package com.example.cleanbookingsbackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record AssignCleanerRequest(String jobId, String adminId, List<String> cleanerId) {

}
