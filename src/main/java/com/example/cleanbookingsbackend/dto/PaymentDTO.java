package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.PaymentStatus;
import com.example.cleanbookingsbackend.model.JobEntity;

import java.util.Date;

public record PaymentDTO(Integer id, Date issueDate, Date dueDate, String jobId, PaymentStatus status, Double price) {
}
