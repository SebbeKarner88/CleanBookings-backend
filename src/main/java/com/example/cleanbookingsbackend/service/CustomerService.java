package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.exception.UsernameIsTakenException;
import com.example.cleanbookingsbackend.exception.ValidationException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;


    public CustomerResponseDTO create(CustomerRegistrationDTO request) throws
            ValidationException,
            UsernameIsTakenException,
            RuntimeException {

        if (!isValidEmailAddress(request.emailAddress()) || !isValidPassword(request.password()))
            throw new ValidationException("Invalid data input");

        if (customerRepository.existsByEmailAddress(request.emailAddress()))
            throw new UsernameIsTakenException("Username is already taken");

        CustomerEntity customer = new CustomerEntity(
                null,
                request.firstName(),
                request.lastName(),
                request.customerType(),
                request.streetAddress(),
                request.postalCode(),
                request.city(),
                request.phoneNumber(),
                request.emailAddress(),
                passwordEncoder.encode(request.password()),
                null
        );

        try {
            customerRepository.save(customer);
            return toDTO(customer);
        } catch (Exception e) {
            throw new RuntimeException("Could not save customer");
        }
    }



    public static CustomerResponseDTO toDTO(CustomerEntity response) {
        return new CustomerResponseDTO(
                response.getFirstName(),
                response.getLastName(),
                response.getCustomerType(),
                response.getStreetAddress(),
                response.getPostalCode(),
                response.getCity(),
                response.getPhoneNumber(),
                response.getEmailAddress()
        );
    }

    private boolean isValidEmailAddress(String email) {
        return email.length() >= 5 && email.contains("@");
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 3;
    }
}
