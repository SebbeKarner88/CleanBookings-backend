package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.DTO.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.DTO.CustomerResponseDTO;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(name = "/api/v1/customer")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(CustomerRegistrationDTO request) {
        return ResponseEntity.ok(toDTO(customerService.create(request)));
    }

    public static CustomerResponseDTO toDTO(CustomerEntity response) {
        return new CustomerResponseDTO(
                response.getFirstName(),
                response.getLastName(),
                response.getCustomerType(),
                response.getStreetAddress(),
                response.getPostalCode(),
                response.getCity(),
                response.getPhoneNumber(),
                response.getEmailAddress()
        );
    }

}
