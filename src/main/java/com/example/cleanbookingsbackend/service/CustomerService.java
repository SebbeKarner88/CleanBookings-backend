package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.AuthenticationResponse;
import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.UsernameIsTakenException;
import com.example.cleanbookingsbackend.exception.ValidationException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthenticationResponse create(CustomerRegistrationDTO request)
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
            return new AuthenticationResponse(customer.getId(), customer.getFirstName());
        } catch (Exception e) {
            throw new RuntimeException("Could not save customer");
        }
    }

    public AuthenticationResponse login(String email, String password) throws CustomerNotFoundException, AuthException {
        if (customerRepository.findByEmailAddress(email).isEmpty())
            throw new CustomerNotFoundException("There is no customer registered with email: " + email);

        CustomerEntity customer = customerRepository.findByEmailAddress(email).get();
        if (!passwordEncoder.matches(password, customer.getPassword()))
            throw new AuthException("The password is incorrect");

        return new AuthenticationResponse(customer.getId(), customer.getFirstName());
    }

    public List<CustomerEntity> listAllCustomers() {
        /* TODO: check if user is admin or else list won't be generated?? */
        return customerRepository.findAll();
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
}
