package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.AdminUserRequest;
import com.example.cleanbookingsbackend.dto.AuthenticationResponse;
import com.example.cleanbookingsbackend.dto.CustomerRegistrationDTO;
import com.example.cleanbookingsbackend.dto.CustomerResponseDTO;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.service.utils.InputValidation;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataField.EMPLOYEE_ID;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataType.STRING;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateInputDataField;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputValidation input;
    private final EmployeeRepository employeeRepository;


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

    public List<CustomerResponseDTO> listAllCustomers(String id)
            throws EmployeeNotFoundException, UnauthorizedCallException {
        List<CustomerResponseDTO> customers = new ArrayList<>();
        if (isAdmin(id))
            customers = customerRepository
                    .findAll()
                    .stream()
                    .map(this::toDTO)
                    .toList();
        return customers;
    }

    public void updateCustomer(AdminUserRequest request, CustomerResponseDTO response)
            throws EmployeeNotFoundException, CustomerNotFoundException, UnauthorizedCallException, NotFoundException {

        if (isAdmin(request.adminId())) {
            authorizedUpdate(request, response);
        }
    }

    public boolean deleteCustomer(AdminUserRequest request)
            throws EmployeeNotFoundException, CustomerNotFoundException, UnauthorizedCallException, NotFoundException {
        if (isAdmin(request.adminId())) {
            authorizedDelete(request);
        }
        return true;
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

    private boolean isAdmin(String id) throws UnauthorizedCallException {
        EmployeeEntity employee = input.validateEmployeeId(id);
        if (!employee.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException("You are not authorized to perform this action.");
        return true;
    }

    private void authorizedUpdate(AdminUserRequest request, CustomerResponseDTO response) throws NotFoundException {
        Optional<CustomerEntity> customer = customerRepository.findById(request.customerId());
        Optional<EmployeeEntity> employee = employeeRepository.findById(request.adminId());

        if (customer.isEmpty() && employee.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.customerId());
        }

        if (customer.isPresent() && employee.isPresent()) {
            customer.get().setFirstName(response.firstName());
            customer.get().setLastName(response.lastName());
            customer.get().setCustomerType(response.customerType());
            customer.get().setStreetAddress(response.streetAddress());
            customer.get().setPostalCode(response.postalCode());
            customer.get().setCity(response.city());
            customer.get().setPhoneNumber(response.phoneNumber());
            customer.get().setEmailAddress(response.emailAddress());
        }
    }

    private void authorizedDelete(AdminUserRequest request) throws NotFoundException {
        Optional<CustomerEntity> customer = customerRepository.findById(request.customerId());
        Optional<EmployeeEntity> employee = employeeRepository.findById(request.adminId());

        if (customer.isEmpty() && employee.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.customerId());
        }

        customerRepository.deleteById(request.customerId());
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

    private CustomerResponseDTO toDTO(CustomerEntity customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getCustomerType(),
                customer.getStreetAddress(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getPhoneNumber(),
                customer.getEmailAddress()
        );
    }

    public CustomerEntity getCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }
}
