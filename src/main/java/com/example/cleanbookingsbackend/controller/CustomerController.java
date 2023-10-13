package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.service.CustomerService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/customer")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
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

    @PutMapping("/updateInfo")
    public ResponseEntity<?> updateCustomerInfo(@RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(customerService.updateCustomerInfo(request));
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }
}
