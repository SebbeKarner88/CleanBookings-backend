package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.AdminEmployeeUpdateRequest;
import com.example.cleanbookingsbackend.dto.JobResponseDTO;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.service.EmployeeService;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/cleaner")
public class CleanerController {
    private final JobService jobService;
    private final EmployeeService employeeService;

    @PreAuthorize("hasRole('client_cleaner')")
    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobsCleaner(@RequestParam String employeeId) {
        try {
            List<JobResponseDTO> jobs = jobService.getAllJobsCleaner(employeeId);
            return ResponseEntity.ok().body(jobs);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('client_admin')")
    @PutMapping("/update-employee")
    public ResponseEntity<?> updateEmployee(@RequestBody AdminEmployeeUpdateRequest request) {
        try {
            employeeService.updateEmployee(request);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException | CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }
}
