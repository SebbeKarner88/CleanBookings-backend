package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CancelJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobResponse;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.exception.NotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
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

    // WIP! NEED TO CHECK IF JOBSTATUS IS OPEN OR ASSIGNED, ELSE WE CANT CANCEL THE JOB.
    // NEED TO CHECK IF CUSTOMER IS THE ONE WHO BOOKED THE JOB HE/SHE IS TRYING TO DELETE.
    @DeleteMapping
    public ResponseEntity<?> cancelJobRequest(@RequestBody CancelJobRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jobService.cancelJobRequest(request));
        } catch (IllegalArgumentException | UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (NotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @GetMapping("/booked-cleanings/{customerId}")
    public ResponseEntity<List<JobEntity>> getBookedCleaningsForCustomer(@PathVariable String customerId) {
        try {
            List<JobEntity> bookedCleanings = jobService.getBookedCleaningsForCustomer(customerId);
            return ResponseEntity.ok(bookedCleanings);
        } catch (CustomerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
