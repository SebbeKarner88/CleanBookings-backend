package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.AuthenticationRequest;
import com.example.cleanbookingsbackend.dto.EmployeeDTO;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.service.EmployeeService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

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

}
