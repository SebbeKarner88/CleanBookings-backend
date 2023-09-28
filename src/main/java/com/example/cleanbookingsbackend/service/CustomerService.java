package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.DTO.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;


    public CustomerEntity create(CustomerRegistrationDTO request) {
        return new CustomerEntity();
    }
}
