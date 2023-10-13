package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.CustomerDataResponse;
import com.example.cleanbookingsbackend.dto.EmployeeDataResponse;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.service.CustomerService;
import com.example.cleanbookingsbackend.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gdpr")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GDPRController {
    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public GDPRController(CustomerService customerService, EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

//    TODO: This is commented-out since it won't compile when the methods referenced doesn't exist
    @GetMapping("/customer-data/{customerId}")
    public ResponseEntity<CustomerDataResponse> getCustomerData(@PathVariable String customerId) {
        try {
            PrivateCustomerEntity customer = customerService.getCustomerById(customerId);
            CustomerDataResponse response = CustomerDataResponse.fromEntity(customer);
            return ResponseEntity.ok(response);
        } catch (CustomerNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/employee-data/{employeeId}")
    public ResponseEntity<EmployeeDataResponse> getEmployeeData(@PathVariable String employeeId) {
        try {
            EmployeeEntity employee = employeeService.getEmployeeById(employeeId);
            EmployeeDataResponse response = EmployeeDataResponse.fromEntity(employee);
            return ResponseEntity.ok(response);
        } catch (EmployeeNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
