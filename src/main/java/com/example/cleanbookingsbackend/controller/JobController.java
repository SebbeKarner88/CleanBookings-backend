package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
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

//    TODO: Commented-out since it won't compile
//    @GetMapping("/booking-history/{customerId}")
//    public ResponseEntity<List<JobDto>> getBookingHistory(@PathVariable String customerId) {
//        System.out.println("Received customerId: " + customerId);
//        List<JobEntity> jobs = jobService.getBookingHistoryForCustomer(customerId);
//
//        // Convert JobEntity objects to JobDto objects
//        List<JobDto> jobDtos = jobs.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(jobDtos);
//    }

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
