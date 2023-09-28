package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InitDataService {
    private final CustomerRepository customerRepository;

    @PostConstruct
    public void initializeData() {
        CustomerEntity customer = new CustomerEntity(
                UUID.randomUUID().toString(),
                "Jane",
                "Doe",
                CustomerType.PRIVATE,
                "Jane Street 1",
                12345,
                "Jane City",
                "076-250 90 80",
                "jane.doe@janecity.com",
                "secret",
                null);
        customerRepository.save(customer);
    }
}
