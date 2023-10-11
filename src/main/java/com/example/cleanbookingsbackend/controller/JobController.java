package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/job")
@CrossOrigin(origins = "*", allowedHeaders = "*")
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

    @PutMapping("/assign-cleaners")
    public ResponseEntity<?> assignCleanerRequest(@RequestBody AssignCleanerRequest request) {
        try {
            jobService.assignCleanerRequest(request);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (EmployeeNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @PutMapping("/executed-cleaning")
    public ResponseEntity<?> executedCleaningRequest(@RequestBody JobUserRequest request) {
        try {
            jobService.executedCleaningRequest(request);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (EmployeeNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @PutMapping("/approve-fail-cleaning")
    public ResponseEntity<?> approvedCleaningRequest(@RequestBody JobApproveRequest request) {
        try {
            jobService.approveDeclineCleaningRequest(request);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException | UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (CustomerNotFoundException | JobNotFoundException | EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @PutMapping("/reissue-failed-cleaning")
    public ResponseEntity<?> reissueFailedCleaningRequest(@RequestBody JobUserRequest request) {
        try {
            jobService.reissueFailedCleaningRequest(request);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (IllegalArgumentException | UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (JobNotFoundException | EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    @DeleteMapping
    public ResponseEntity<?> cancelJobRequest(@RequestBody JobUserRequest request) {
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



    @GetMapping("/cleanings/{customerId}")
    public ResponseEntity<List<JobDto>> getCleaningsByStatus(
            @PathVariable String customerId,
            @RequestParam(required = false) JobStatus status) {
        System.out.println("Received customerId: " + customerId);

        // Check if the status parameter is provided
        if (status != null) {
            List<JobEntity> jobs = jobService.getCleaningsByStatusAndCustomerId(customerId, status);
            // Convert JobEntity objects to JobDto objects
            List<JobDto> jobDtos = jobs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDtos);
        } else {
            // Handle the "ALL" option (status parameter is not provided)
            List<JobEntity> jobs = jobService.getAllCleaningsForCustomer(customerId);
            // Convert JobEntity objects to JobDto objects
            List<JobDto> jobDtos = jobs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDtos);
        }
    }

    @GetMapping("/cleanings/employee/{employeeId}")
    public ResponseEntity<List<JobDto>> getCleaningsByStatusWithRole(
            @PathVariable String employeeId,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) Role role) {
        System.out.println("Received employeeId: " + employeeId);

        if (status != null) {
            if (role == Role.ADMIN) {
                // Admin can view jobs for any cleaner
                List<JobEntity> jobs = jobService.getCleaningsByStatus(status);
                List<JobDto> jobDtos = jobs.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(jobDtos);
            } else {
                // Cleaners can view their own jobs
                List<JobEntity> jobs = jobService.getCleaningsByStatusAndEmployeeId(employeeId, status);
                List<JobDto> jobDtos = jobs.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(jobDtos);
            }
        } else if (role == Role.ADMIN) {
            // Admin can view all jobs for all cleaners
            List<JobEntity> jobs = jobService.getAllCleaningsForAllCleaners();
            List<JobDto> jobDtos = jobs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDtos);
        } else {
            // Cleaners can view all their jobs
            List<JobEntity> jobs = jobService.getAllCleaningsForCleaner(employeeId);
            List<JobDto> jobDtos = jobs.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(jobDtos);
        }
    }

    @GetMapping("/booked-cleanings/{customerId}")
    public ResponseEntity<List<JobDto>> getBookedCleanings(@PathVariable String customerId) {
        System.out.println("Received customerId: " + customerId);
        List<JobEntity> jobs = jobService.getBookedCleaningsForCustomer(customerId);
        // Convert JobEntity objects to JobDto objects
        List<JobDto> jobDtos = jobs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobDtos);
    }

    @GetMapping("/booking-history/{customerId}")
    public ResponseEntity<List<JobDto>> getBookingHistory(@PathVariable String customerId) {
        System.out.println("Received customerId: " + customerId);
        List<JobEntity> jobs = jobService.getBookingHistoryForCustomer(customerId);

        // Convert JobEntity objects to JobDto objects
        List<JobDto> jobDtos = jobs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(jobDtos);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> deleteJob(
            @PathVariable String jobId,
            @RequestParam String employeeId
    ) {
        try {
            jobService.deleteJob(employeeId, jobId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (IllegalArgumentException | UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    private JobDto convertToDto(JobEntity jobEntity) {
        JobDto jobDto = new JobDto();
        jobDto.setId(jobEntity.getId());
        jobDto.setBookedDate(jobEntity.getBookedDate());
        jobDto.setType(jobEntity.getType());
        jobDto.setMessage(jobEntity.getMessage());
        jobDto.setStatus(jobEntity.getStatus());

        return jobDto;
    }
}
