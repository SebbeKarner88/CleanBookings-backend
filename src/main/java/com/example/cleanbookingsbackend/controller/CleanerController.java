package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.JobResponseDTO;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/cleaner")
public class CleanerController {
    private final JobService jobService;

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
}
