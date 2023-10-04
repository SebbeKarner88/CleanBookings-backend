package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CustomerDataResponse;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gdpr")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GDPRController {
    private final CustomerService customerService;

    public GDPRController(CustomerService customerService) {
        this.customerService = customerService;
    }

//    TODO: This is commented-out since it won't compile when the methods referenced doesn't exist
    @GetMapping("/customer-data/{customerId}")
    public ResponseEntity<CustomerDataResponse> getCustomerData(@PathVariable String customerId) {
        try {
            CustomerEntity customer = customerService.getCustomerById(customerId);
            CustomerDataResponse response = CustomerDataResponse.fromEntity(customer);
            return ResponseEntity.ok(response);
        } catch (CustomerNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
