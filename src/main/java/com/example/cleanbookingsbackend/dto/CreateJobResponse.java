package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.JobType;
import lombok.Builder;


@Builder
public record CreateJobResponse(String jobId, JobType jobType, String date, Customer customer) {
    public record Customer(String name, String phoneNumber, String emailAdress, Adress adress){}
    public record Adress(String streetAdress, int postalCode, String city){}
}
