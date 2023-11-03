package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.keycloak.api.KeycloakAPI;
import com.example.cleanbookingsbackend.keycloak.models.tokenEntity.KeycloakTokenEntity;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.service.utils.InputValidation;

import com.example.cleanbookingsbackend.service.utils.MailSenderService;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder encoder;
    private final InputValidation input;
    private final EmployeeRepository employeeRepository;
    private final KeycloakAPI keycloakAPI;
    private final JwtDecoder jwtDecoder;
    private final MailSenderService mailSender;

    public String create(CustomerRegistrationDTO request)
            throws ValidationException,
            UsernameIsTakenException,
            RuntimeException,
            SocSecNumberIsTakenException {

        validateCustomerInputData(request);

        if (!input.isValidEmailAddress(request.emailAddress()) || !input.isValidPassword(request.password()))
            throw new ValidationException("Invalid email/password data");

        if (customerRepository.existsByEmailAddress(request.emailAddress()))
            throw new UsernameIsTakenException("Username is already taken");

        if (checkIfSocSecNumberExists(request.personNumber()))
            throw new SocSecNumberIsTakenException("Social security number already exists");

        PrivateCustomerEntity customer = customerBuilder(request);

        try {
            customerRepository.save(keycloakAPI.addCustomerKeycloak(customer, request.password()));
            return customer.getId();
        } catch (Exception e) {
            throw new RuntimeException("Could not save customer");
        }
    }

    public AuthenticationResponse login(String email, String password) throws CustomerNotFoundException, AuthException {
        if (email.isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (password.isBlank())
            throw new IllegalArgumentException("Password is required.");

        customerRepository.findByEmailAddress(email).orElseThrow(
                () -> new CustomerNotFoundException("There is no customer registered with email: " + email)
        );

        KeycloakTokenEntity response = keycloakAPI.loginKeycloak(email, password);
        String accessToken = response.getAccess_token();
        String refreshToken = response.getRefresh_token();

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            String customerId = jwt.getSubject();
            String name = jwt.getClaimAsString("given_name");
            String role = keycloakAPI.getUserRole(jwt);

            return new AuthenticationResponse(
                    customerId,
                    name,
                    accessToken,
                    refreshToken,
                    role
            );
        } catch (Error error) {
            throw new Error(error.getMessage());
        }
    }

    public AuthenticationResponse refresh(String token) {
        if (token.isBlank())
            throw new IllegalArgumentException("Missing header. Refresh token is required.");

        KeycloakTokenEntity response = keycloakAPI.refreshToken(token);
        String accessToken = response.getAccess_token();
        String refreshToken = response.getRefresh_token();

        Jwt jwt = jwtDecoder.decode(accessToken);
        String customerId = jwt.getSubject();
        String name = jwt.getClaimAsString("given_name");
        String role = keycloakAPI.getUserRole(jwt);

        return new AuthenticationResponse(
                customerId,
                name,
                accessToken,
                refreshToken,
                role
        );
    }

    public void logout(String token) {
        if (token.isBlank())
            throw new IllegalArgumentException("Missing header. Refresh token is required.");
        keycloakAPI.logoutKeycloak(token);
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

    public boolean updateCustomerInfo(String id, UserUpdateRequest request)
            throws RuntimeException {

        PrivateCustomerEntity customer = input.validateCustomerId(id);
        PrivateCustomerEntity updatedCustomer = updateCustomer(customer, request);
        customerRepository.save(keycloakAPI.updateCustomerKeycloak(updatedCustomer));
        return true;
    }

//    TODO: Needs to be adapted to get password from Keycloak DB
//    public boolean updateCustomerPassword(String customerId, PasswordUpdateRequest request)
//            throws UnauthorizedCallException {
//        PrivateCustomerEntity customer = input.validateCustomerId(customerId);
//        if (!encoder.matches(request.oldPassword(), customer.getPassword()))
//            throw new UnauthorizedCallException("Invalid password");
//        customer.setPassword(encoder.encode(request.newPassword()));
//        customerRepository.save(customer);
//        return true;
//    }

    public boolean updateCustomerAdmin(AdminUserUpdateRequest request)
            throws EmployeeNotFoundException, CustomerNotFoundException, UnauthorizedCallException, NotFoundException {

        if (isAdmin(request.adminId())) {
            authorizedUpdate(request);
        }
        return true;
    }

    public void deleteCustomer(String adminId, String customerId)
            throws EmployeeNotFoundException, CustomerNotFoundException, UnauthorizedCallException, NotFoundException {
        if (isAdmin(adminId)) {
            authorizedDelete(adminId, customerId);
        }
    }

    public boolean contactUsForm(ContactRequest request) {
        try {
            mailSender.sendEmailConfirmationMessageReceived(request);
        } catch (Exception e) {
            throw new RuntimeException("Could not send message.");
        }

        return true;
    }

    // ##### Validation #####

    private void validateCustomerInputData(CustomerRegistrationDTO request) throws ValidationException {
        if (request.firstName().isBlank())
            throw new ValidationException("First name is required");
        if (request.lastName().isBlank())
            throw new ValidationException("Last name is required.");
        if (request.personNumber().isBlank())
            throw new ValidationException("Social security is required.");
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

    private void authorizedUpdate(AdminUserUpdateRequest request) throws NotFoundException, RuntimeException {
        Optional<PrivateCustomerEntity> customerOptional = customerRepository.findById(request.customerId());
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findById(request.adminId());

        if (customerOptional.isEmpty() && employeeOptional.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.customerId());
        }

        if (customerOptional.isPresent() && employeeOptional.isPresent()) {
            PrivateCustomerEntity customer = customerOptional.orElse(null);
            // check if response fields are not null before updating
            if (request.firstName() != null) {
                customer.setFirstName(request.firstName());
            }
            if (request.lastName() != null) {
                customer.setLastName(request.lastName());
            }
            if (request.customerType() != null) {
                customer.setCustomerType(request.customerType());
            }
            if (request.streetAddress() != null) {
                customer.setStreetAddress(request.streetAddress());
            }
            if (request.postalCode() != null) {
                customer.setPostalCode(request.postalCode());
            }
            if (request.city() != null) {
                customer.setCity(request.city());
            }
            if (request.phoneNumber() != null) {
                customer.setPhoneNumber(request.phoneNumber());
            }
            if (request.emailAddress() != null) {
                customer.setEmailAddress(request.emailAddress());
            }
            customerRepository.save(keycloakAPI.updateCustomerKeycloak(customer));
        }
    }

    private void authorizedDelete(String adminId, String customerId) throws NotFoundException, UnauthorizedCallException {
        Optional<PrivateCustomerEntity> customer = customerRepository.findById(customerId);
        Optional<EmployeeEntity> employee = employeeRepository.findById(adminId);

        if (customer.isEmpty() && employee.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + customerId);
        } else if (customer.isPresent()) {
            if (customer.get().getJobs().isEmpty()) {
                try{
                    keycloakAPI.deleteUserKeycloak(customerId);
                } catch (Exception e) {
                    throw new RuntimeException("Could not delete customer. Error: " + e);
                }
                customerRepository.deleteById(customerId);
            } else {
                throw new UnauthorizedCallException("This customer has one or more active bookings.");
            }
        }
    }

    private PrivateCustomerEntity updateCustomer(PrivateCustomerEntity customer, UserUpdateRequest request) {
        if (request.firstName() != null) {
            customer.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            customer.setLastName(request.lastName());
        }
        if (request.streetAddress() != null) {
            customer.setStreetAddress(request.streetAddress());
        }
        if (request.postalCode() != null) {
            customer.setPostalCode(request.postalCode());
        }
        if (request.city() != null) {
            customer.setCity(request.city());
        }
        if (request.phoneNumber() != null) {
            customer.setPhoneNumber(request.phoneNumber());
        }
        if (request.emailAddress() != null) {
            customer.setEmailAddress(request.emailAddress());
        }
        return customer;
    }

    private boolean checkIfSocSecNumberExists(String number) {
        AtomicBoolean exists = new AtomicBoolean(false);
        customerRepository.findAll()
                .forEach(customer -> {
                    if (encoder.matches(number, customer.getPersonNumber()))
                        exists.set(true);
                });
        return exists.get();
    }

    // ##### Builder #####
    public PrivateCustomerEntity customerBuilder(CustomerRegistrationDTO request) {
        return new PrivateCustomerEntity(
                null,
                request.firstName(),
                request.lastName(),
                request.personNumber(),
                request.customerType(),
                request.streetAddress(),
                request.postalCode(),
                request.city(),
                request.phoneNumber(),
                request.emailAddress(),
                null
        );
    }

    private CustomerResponseDTO toDTO(PrivateCustomerEntity customer) {
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

    public PrivateCustomerEntity getCustomerById(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
    }
}
