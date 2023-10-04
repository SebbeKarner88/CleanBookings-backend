package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.AuthenticationRequest;
import com.example.cleanbookingsbackend.dto.AuthenticationResponse;
import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.UsernameIsTakenException;
import com.example.cleanbookingsbackend.exception.ValidationException;
import com.example.cleanbookingsbackend.service.CustomerService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/customer")
public class CustomerController {


    private final CustomerService customerService;


    @PostMapping
    public ResponseEntity<?> create(@RequestBody CustomerRegistrationDTO request) {
        try {
            AuthenticationResponse response = customerService.create(request);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.customerId())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } catch (ValidationException | UsernameIsTakenException | RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(customerService.login(request.email(), request.password()));
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (AuthException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }
}
