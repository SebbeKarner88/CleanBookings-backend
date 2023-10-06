package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.AdminRequest;
import com.example.cleanbookingsbackend.dto.CustomerDataResponse;
import com.example.cleanbookingsbackend.dto.JobResponseDTO;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.service.CustomerService;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/admin")
public class AdminController {
    private final JobService jobService;
    private final CustomerService customerService;

    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobs(@RequestParam String employeeId) {
        try {
            List<JobResponseDTO> jobs = jobService.getAllJobs(employeeId);
            return ResponseEntity.ok().body(jobs);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> listAllCustomers() {
        try {
            List<CustomerDataResponse> customerDTOList =
                    customerService.listAllCustomers()
                            .stream()
                            .map(this::toDTO)
                            .collect(Collectors.toList());

            return ResponseEntity.ok(customerDTOList);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    private CustomerDataResponse toDTO(CustomerEntity customerEntity) {
        return new CustomerDataResponse(
                customerEntity.getId(),
                customerEntity.getFirstName(),
                customerEntity.getLastName(),
                customerEntity.getCustomerType(),
                customerEntity.getStreetAddress(),
                customerEntity.getPostalCode(),
                customerEntity.getCity(),
                customerEntity.getPhoneNumber(),
                customerEntity.getEmailAddress()
        );
    }
}
