package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.EmployeeAuthenticationResponse;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAuthenticationResponse login(String email, String password) throws EmployeeNotFoundException, AuthException {
        EmployeeEntity employee = employeeRepository.findByEmailAddress(email).orElseThrow(
                () -> new EmployeeNotFoundException("There is no employee registered with email: " + email)
        );

        if (!passwordEncoder.matches(password, employee.getPassword()))
            throw new AuthException("The password is incorrect");

        return new EmployeeAuthenticationResponse(employee.getId(), employee.getEmailAddress(), employee.getRole());
    }

    public EmployeeEntity getEmployeeById(String employeeId) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);

        if (employee.isPresent()) {
            return employee.get();
        } else {
            throw new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
        }
    }
}
