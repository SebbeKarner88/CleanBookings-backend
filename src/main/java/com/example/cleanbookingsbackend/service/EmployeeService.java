package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository userRepository;


}
