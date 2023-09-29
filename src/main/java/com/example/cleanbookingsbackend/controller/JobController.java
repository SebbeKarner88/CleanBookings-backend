package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CancelJobRequest;
import com.example.cleanbookingsbackend.dto.CancelJobResponse;
import com.example.cleanbookingsbackend.dto.CreateJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobResponse;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/job")
public class JobController {
    private final JobService jobService;

    @GetMapping("/booked-cleanings/{customerId}")
    public ResponseEntity<List<JobEntity>> getBookedCleaningsForCustomer(@PathVariable String customerId) {
        try {
            List<JobEntity> bookedCleanings = jobService.getBookedCleaningsForCustomer(customerId);
            return ResponseEntity.ok(bookedCleanings);
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<?> createJobRequest(@RequestBody CreateJobRequest request) {
        try {
            CreateJobResponse response = jobService.createJobRequest(request);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.jobId())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> cancelJobRequest(@RequestBody CancelJobRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jobService.cancelJobRequest(request));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (CustomerNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }
}
