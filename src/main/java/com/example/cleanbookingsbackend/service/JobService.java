package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

}
