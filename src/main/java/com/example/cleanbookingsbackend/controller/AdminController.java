package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.NotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.service.CustomerService;
import com.example.cleanbookingsbackend.service.EmployeeService;
import com.example.cleanbookingsbackend.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/admin")
public class AdminController {
    private final JobService jobService;
    private final CustomerService customerService;
    private final EmployeeService employeeService;

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

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins(@RequestParam String employeeId) {
        try {
            List<EmployeeResponseDTO> admins = employeeService.getAllAdmins(employeeId);
            return ResponseEntity.ok().body(admins);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @GetMapping("/cleaners")
    public ResponseEntity<?> getAllCleaners(@RequestParam String employeeId) {
        try {
            List<EmployeeResponseDTO> admins = employeeService.getAllCleaners(employeeId);
            return ResponseEntity.ok().body(admins);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @PutMapping("/updateCustomer")
    public ResponseEntity<?> updateCustomer(@RequestBody AdminUserUpdateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(customerService.updateCustomerAdmin(request));
        } catch (EmployeeNotFoundException | CustomerNotFoundException | NotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCustomer(@RequestParam String adminId,
                                            @RequestParam String customerId) {
        try {
            customerService.deleteCustomer(adminId, customerId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException | CustomerNotFoundException | NotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @DeleteMapping("/cleaner/{cleanerId}")
    public ResponseEntity<?> deleteCleaner(
            @PathVariable String cleanerId,
            @RequestParam String employeeId
    ) {
        try {
            employeeService.deleteCleaner(employeeId, cleanerId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

    @DeleteMapping("/admin/{adminId}")
    public ResponseEntity<?> deleteAdmin(
            @PathVariable String adminId,
            @RequestParam String employeeId
    ) {
        try {
            employeeService.deleteCleaner(employeeId, adminId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }
}
