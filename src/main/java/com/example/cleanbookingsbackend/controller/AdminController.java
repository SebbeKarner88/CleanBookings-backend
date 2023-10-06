package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CustomerDataResponse;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
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

    @GetMapping("/customers")
    public ResponseEntity<?> listAllCustomers(@RequestParam String employeeId) {
        try {
            List<CustomerResponseDTO> customerDTOList = customerService.listAllCustomers(employeeId);
            return ResponseEntity.ok().body(customerDTOList);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }
}
