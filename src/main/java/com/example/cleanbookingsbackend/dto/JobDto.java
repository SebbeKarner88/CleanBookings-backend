package com.example.cleanbookingsbackend.dto;

import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import lombok.Data;
import lombok.Setter;

import java.util.Date;

@Data
public class JobDto {
    private String id;
    private Date bookedDate;
    private JobType type;
    private String message;
    private JobStatus status;
}
