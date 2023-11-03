package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.keycloak.api.KeycloakAPI;
import com.example.cleanbookingsbackend.keycloak.models.tokenEntity.KeycloakTokenEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.service.utils.InputValidation;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataField.EMPLOYEE_ID;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataType.STRING;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateInputDataField;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepository;
    private final InputValidation input;
    private final KeycloakAPI keycloakAPI;
    private final JwtDecoder jwtDecoder;

    private static final String UNAUTHORIZED_CALL_MESSAGE = "You are not authorized to perform this action.";

    public EmployeeAuthenticationResponse login(String email, String password) throws EmployeeNotFoundException, AuthException {
        employeeRepository.findByEmailAddress(email).orElseThrow(
                () -> new EmployeeNotFoundException("There is no employee registered with email: " + email)
        );

        KeycloakTokenEntity response = keycloakAPI.loginKeycloak(email, password);
        String accessToken = response.getAccess_token();
        String refreshToken = response.getRefresh_token();

        try {
            Jwt jwt = jwtDecoder.decode(accessToken);
            String employeeId = jwt.getSubject();
            String emailAddress = jwt.getClaimAsString("email");
            String role = keycloakAPI.getUserRole(jwt);

            return new EmployeeAuthenticationResponse(
                    employeeId,
                    emailAddress,
                    role,
                    accessToken,
                    refreshToken
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
        String employeeId = jwt.getSubject();
        String emailAddress = jwt.getClaimAsString("email");
        String role = keycloakAPI.getUserRole(jwt);

        return new AuthenticationResponse(
                employeeId,
                emailAddress,
                role,
                accessToken,
                refreshToken
        );
    }

    public void logout(String token) {
        if (token.isBlank())
            throw new IllegalArgumentException("Missing header. Refresh token is required.");
        keycloakAPI.logoutKeycloak(token);
    }

    public CreateEmployeeResponse createEmployeeRequest(CreateEmployeeRequest request)
            throws ValidationException, UsernameIsTakenException, RuntimeException {

        validateEmployeeInputData(request);

        if (!input.isValidEmailAddress(request.emailAddress()))
            throw new ValidationException("Invalid email/password data");

        if (employeeRepository.existsByEmailAddress(request.emailAddress()))
            throw new UsernameIsTakenException("Username is already taken");

        EmployeeEntity employee = employeeBuilder(request);

        try {
            employeeRepository.save(keycloakAPI.addEmployeeKeycloak(employee));
            return new CreateEmployeeResponse(
                    employee.getId(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getPhoneNumber(),
                    employee.getRole(),
                    employee.getEmailAddress());
        } catch (Exception e) {
            throw new RuntimeException("Could not save customer");
        }
    }


    private EmployeeEntity employeeBuilder(CreateEmployeeRequest request) {
        return new EmployeeEntity(
                null,
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.role(),
                request.emailAddress(),
                null
        );
    }

    private void validateEmployeeInputData(CreateEmployeeRequest request)
            throws ValidationException {
        if (request.firstName().isBlank())
            throw new ValidationException("First name is required");
        if (request.lastName().isBlank())
            throw new ValidationException("Last name is required.");
        if (request.phoneNumber().isBlank())
            throw new ValidationException("Phone number is required.");
        if (request.emailAddress().isBlank())
            throw new ValidationException("Email is required");
    }

    public EmployeeEntity getEmployeeById(String employeeId) {
        Optional<EmployeeEntity> employee = employeeRepository.findById(employeeId);

        if (employee.isPresent()) {
            return employee.get();
        } else {
            throw new EmployeeNotFoundException("Employee not found with ID: " + employeeId);
        }
    }

    public List<EmployeeDTO> getAllAvailableEmployees(String employeeId, String jobId) throws EmployeeNotFoundException, UnauthorizedCallException, JobNotFoundException {
        List<EmployeeDTO> availableEmployees = new ArrayList<>();
        JobEntity requestedJob = input.validateJobId(jobId);

        if (isAdmin(employeeId)) {
            checkIfAvailable(availableEmployees, requestedJob);
        }
        return availableEmployees;
    }

    private void checkIfAvailable(List<EmployeeDTO> availableEmployees, JobEntity requestedJob) {
        List<EmployeeEntity> allCleaners = employeeRepository
                .findAll()
                .stream()
                .filter(employee -> employee.getRole().equals(Role.CLEANER))
                .toList();

        allCleaners.forEach(employee -> {
            List<JobEntity> jobs = jobRepository.findAllByEmployeeId(employee.getId());
            boolean isAvailable = jobs
                    .stream()
                    .noneMatch(job -> job.getBookedDate().equals(requestedJob.getBookedDate()));
/*
            If the employee isn't assigned to any job on that specific date,
            or if the employee already was assigned to this job (in the case of a NOT_APPROVED job)
            add them to the list of available employees
*/
            if (isAvailable || requestedJob.getEmployee().contains(employee))
                availableEmployees.add(convertToEmployeeDTO(employee));
        });
    }

    public List<EmployeeResponseDTO> getAllAdmins(String employeeId)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        List<EmployeeResponseDTO> admins = new ArrayList<>();
        if (isAdmin(employeeId))
            admins = employeeRepository
                    .findAllByRole(Role.ADMIN)
                    .stream().map(this::convertToEmployeeResponseDTO)
                    .toList();
        return admins;
    }

    public List<EmployeeResponseDTO> getAllCleaners(String employeeId)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        List<EmployeeResponseDTO> cleaners = new ArrayList<>();
        if (isAdmin(employeeId))
            cleaners = employeeRepository
                    .findAllByRole(Role.CLEANER)
                    .stream().map(this::convertToEmployeeResponseDTO)
                    .toList();
        return cleaners;
    }

    public List<EmployeeResponseDTO> getAllCleanersInfo() {
        return employeeRepository
                .findAllByRole(Role.CLEANER)
                .stream().map(this::convertToEmployeeResponseDTO)
                .toList();
    }

    public void deleteCleaner(String employeeId, String cleanerId)
            throws UnauthorizedCallException, EmployeeNotFoundException, RuntimeException {
        if (isAdmin(employeeId)) {
            input.validateEmployeeId(cleanerId);
            try {
                keycloakAPI.deleteUserKeycloak(cleanerId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete Cleaner. Error: " + e);
            }
            employeeRepository.deleteById(cleanerId);
        }
    }


    public void updateEmployee(AdminEmployeeUpdateRequest request) throws UnauthorizedCallException {
        // Check if the user is authorized to make updates
        checkAuthorizedToUpdate(request.adminId(), request.employeeId());

        EmployeeEntity employee = input.validateEmployeeId(request.employeeId());
        if (request.firstName() != null)
            employee.setFirstName(request.firstName());
        if (request.lastName() != null)
            employee.setLastName(request.lastName());
        if (request.emailAddress() != null)
            employee.setEmailAddress(request.emailAddress());
        if (request.phoneNumber() != null)
            employee.setPhoneNumber(request.phoneNumber());

        employeeRepository.save(keycloakAPI.updateEmployeeKeycloak(employee));
    }

    private void checkAuthorizedToUpdate(String loggedInUserId, String employeeIdToUpdate)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        // If the logged-in user ID is null, throw an exception
        if (loggedInUserId == null) {
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        }

        // Check if the logged-in user is an admin or the same as the employee being updated
        if (isAdmin(loggedInUserId) || loggedInUserId.equals(employeeIdToUpdate)) {
            return;
        }

        throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
    }

    public boolean isAdmin(String id)
            throws EmployeeNotFoundException {
        validateInputDataField(EMPLOYEE_ID, STRING, id);
        EmployeeEntity employee = input.validateEmployeeId(id);
        return employee.getRole().equals(Role.ADMIN);
    }

    private EmployeeDTO convertToEmployeeDTO(EmployeeEntity employee) {
        return new EmployeeDTO(employee.getId(), (employee.getFirstName()) + " " + employee.getLastName());
    }

    private EmployeeResponseDTO convertToEmployeeResponseDTO(EmployeeEntity employee) {
        return new EmployeeResponseDTO(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getEmailAddress(), employee.getPhoneNumber());
    }

//    TODO: Needs to be adapted for Keycloak integration
//    public boolean updateEmployeePassword(String employeeId, PasswordUpdateRequest request)
//            throws UnauthorizedCallException {
//        EmployeeEntity employee = input.validateEmployeeId(employeeId);
//        if (!encoder.matches(request.oldPassword(), employee.getPassword()))
//            throw new UnauthorizedCallException("Invalid password");
//        employee.setPassword(encoder.encode(request.newPassword()));
//        employeeRepository.save(employee);
//        return true;
//    }
}
