package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.exception.UsernameIsTakenException;
import com.example.cleanbookingsbackend.exception.ValidationException;
import com.example.cleanbookingsbackend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
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
        try {
            CustomerResponseDTO response = customerService.create(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (ValidationException | UsernameIsTakenException | RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.valueOf(String.valueOf(ex))).body(null);
        }
    }

}
