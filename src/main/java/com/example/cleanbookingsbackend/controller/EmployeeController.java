package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.service.EmployeeService;
import com.example.cleanbookingsbackend.service.JobService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/employee")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final JobService jobService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(employeeService.login(request.email(), request.password()));
        } catch (CustomerNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (AuthException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(@RequestHeader String refresh_token) {
        try {
            return ResponseEntity.ok(employeeService.refresh(refresh_token));
        }  catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader String refresh_token) {
        try {
            employeeService.logout(refresh_token);
            return ResponseEntity.noContent().build();
        }  catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('client_admin')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateEmployeeRequest request) {
        try {
            CreateEmployeeResponse response = employeeService.createEmployeeRequest(request);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(response.id())
                    .toUri();
            return ResponseEntity.created(location).body(response);
        } catch (UsernameIsTakenException | RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }

    // THIS CALL IS PURELY FOR FETCHING THE CLEANERS FOR OUR ABOUT US PAGE.
    @GetMapping("/getAllCleaners")
    public ResponseEntity<?> getAllCleanersInfo() {
        try {
            List<EmployeeResponseDTO> cleaners = employeeService.getAllCleanersInfo();
            return ResponseEntity.ok().body(cleaners);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    @PreAuthorize("hasRole('client_admin')")
    @GetMapping
    public ResponseEntity<?> getAllAvailableEmployees(
            @RequestParam String employeeId,
            @RequestParam String jobId
    ) {
        try {
            List<EmployeeDTO> employees = employeeService.getAllAvailableEmployees(employeeId, jobId);
            return ResponseEntity.ok().body(employees);
        } catch (EmployeeNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('client_admin', 'client_cleaner')")
    @GetMapping("/jobs")
    public ResponseEntity<?> getAllJobsByEmployee(@RequestParam String employeeId) {
        try {
            List<JobResponseDTO> jobs = jobService
                    .getAllCleaningsForCleaner(employeeId)
                    .stream()
                    .map(this::convertToJobResponseDTO)
                    .toList();
            return ResponseEntity.ok().body(jobs);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        }
    }

    private JobResponseDTO convertToJobResponseDTO(JobEntity job) {
        return new JobResponseDTO(
                job.getId(),
                job.getType().toString(),
                job.getStatus().toString(),
                job.getMessage(),
                job.getCustomer().getId(),
                job.getEmployee().stream().map(employee -> employee.getFirstName().concat(" ").concat(employee.getLastName())).toList()
        );
    }


   @PreAuthorize("hasAnyRole('client_admin', 'client_cleaner')")
    @PutMapping("updatePassword/{id}")
    public ResponseEntity<?> updateEmployeePassword(@PathVariable("id") String employeeId,
                                                    @RequestBody PasswordUpdateRequest request) {
        try {
           return ResponseEntity.status(HttpStatus.OK).body(employeeService.updateEmployeePassword(employeeId, request.newPassword()));
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong, and I don't know why...");
        }
    }
}
