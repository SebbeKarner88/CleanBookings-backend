package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
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


    public CustomerResponseDTO create(CustomerRegistrationDTO request)
            throws ValidationException,
            UsernameIsTakenException,
            RuntimeException {

        validateCustomerInputData(request);

        if (!isValidEmailAddress(request.emailAddress()) || !isValidPassword(request.password()))
            throw new ValidationException("Invalid email/password data");

        if (customerRepository.existsByEmailAddress(request.emailAddress()))
            throw new UsernameIsTakenException("Username is already taken");

        CustomerEntity customer = customerBuilder(request);

        try {
            customerRepository.save(customer);
            return toDTO(customer);
        } catch (Exception e) {
            throw new RuntimeException("Could not save customer");
        }
    }

    //###### DTO #######
    public static CustomerResponseDTO toDTO(CustomerEntity response) {
        return new CustomerResponseDTO(
                response.getId(),
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

    // ##### Validation #####
    private boolean isValidEmailAddress(String email) {
        return email.length() >= 5 && email.contains("@");
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 3;
    }

    private void validateCustomerInputData(CustomerRegistrationDTO request) throws ValidationException {
        if (request.firstName().isBlank())
            throw new ValidationException("First name is required");
        if (request.lastName().isBlank())
            throw new ValidationException("Last name is required.");
        if (request.customerType() == null)
            throw new ValidationException("Type is required.");
        if (request.streetAddress().isBlank())
            throw new ValidationException("Adress is required");
        if (request.postalCode() == null)
            throw new ValidationException("Postal code is required.");
        if (request.city().isBlank())
            throw new ValidationException("City is required");
        if (request.phoneNumber().isBlank())
            throw new ValidationException("Phone number is required.");
        if (request.emailAddress().isBlank())
            throw new ValidationException("Email is required");
        if (request.password().isBlank())
            throw new ValidationException("Password is required.");
    }

    // ##### Builder #####
    public CustomerEntity customerBuilder(CustomerRegistrationDTO request) {
        return new CustomerEntity(
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
    }

    public CustomerEntity getCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }
}
