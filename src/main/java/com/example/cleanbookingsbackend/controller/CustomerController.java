package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.service.CustomerService;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            String id = customerService.create(request);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();
            return ResponseEntity.created(location).build();
        } catch (ValidationException | SocSecNumberIsTakenException | UsernameIsTakenException | RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(customerService.login(request.email(), request.password()));
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (AuthException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestHeader String refresh_token) {
        try {
            return ResponseEntity.ok(customerService.refresh(refresh_token));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String refresh_token) {
        try {
            customerService.logout(refresh_token);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('client_customer')")
    @PutMapping("updateCustomer/{id}")
    public ResponseEntity<?> updateCustomerInfo(@PathVariable("id") String id,
                                                @RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(customerService.updateCustomerInfo(id, request));
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @PreAuthorize("hasRole('client_customer')")
    @PutMapping("updatePassword/{id}")
    public ResponseEntity<?> updateCustomerPassword(
            @PathVariable("id") String customerId,
            @RequestBody PasswordUpdateRequest request,
            HttpServletRequest servletRequest) {
        try {
            customerService.updateCustomerPassword(customerId, request, servletRequest);
            return ResponseEntity.noContent().build();
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("receive-msg")
    public ResponseEntity<?> contactUsForm(@RequestBody ContactRequest request) {
        return ResponseEntity.ok(customerService.contactUsForm(request));
    }
}
