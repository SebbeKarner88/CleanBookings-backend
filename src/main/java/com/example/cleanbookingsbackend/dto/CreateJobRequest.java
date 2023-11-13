package com.example.cleanbookingsbackend.dto;

public record CreateJobRequest(String customerId, String timeslot, String type, String date, String message) {
}
