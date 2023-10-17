package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
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
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;
    private final InputValidation input;

    private static final String UNAUTHORIZED_CALL_MESSAGE = "You are not authorized to perform this action.";

    public EmployeeAuthenticationResponse login(String email, String password) throws EmployeeNotFoundException, AuthException {
        EmployeeEntity employee = employeeRepository.findByEmailAddress(email).orElseThrow(
                () -> new EmployeeNotFoundException("There is no employee registered with email: " + email)
        );

        if (!passwordEncoder.matches(password, employee.getPassword()))
            throw new AuthException("The password is incorrect");

        return new EmployeeAuthenticationResponse(employee.getId(), employee.getEmailAddress(), employee.getRole());
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
            employeeRepository.save(employee);
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
                passwordEncoder.encode("password"),
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
        if(isAdmin(employeeId))
            admins = employeeRepository
                    .findAllByRole(Role.ADMIN)
                    .stream().map(this::convertToEmployeeResponseDTO)
                    .toList();
        return admins;
    }

    public List<EmployeeResponseDTO> getAllCleaners(String employeeId)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        List<EmployeeResponseDTO> cleaners = new ArrayList<>();
        if(isAdmin(employeeId))
            cleaners = employeeRepository
                    .findAllByRole(Role.CLEANER)
                    .stream().map(this::convertToEmployeeResponseDTO)
                    .toList();
        return cleaners;
    }


    public void deleteCleaner(String employeeId, String cleanerId)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        if(isAdmin(employeeId)) {
            input.validateEmployeeId(cleanerId);
            employeeRepository.deleteById(cleanerId);
        }
    }

    public void updateEmployee(AdminEmployeeUpdateRequest request) throws UnauthorizedCallException {
        if (isAdmin((request.adminId()))) {
            EmployeeEntity employee = input.validateEmployeeId(request.employeeId());
            if (request.firstName() != null)
                employee.setFirstName(request.firstName());
            if (request.lastName() != null)
                employee.setLastName(request.lastName());
            if (request.emailAddress() != null)
                employee.setEmailAddress(request.emailAddress());
            if (request.phoneNumber() != null)
                employee.setPhoneNumber(request.phoneNumber());
            employeeRepository.save(employee);
        }
    }

    private boolean isAdmin(String id)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        validateInputDataField(EMPLOYEE_ID, STRING, id);
        EmployeeEntity employee = input.validateEmployeeId(id);
        if (!employee.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        return true;
    }

    private EmployeeDTO convertToEmployeeDTO(EmployeeEntity employee) {
        return new EmployeeDTO(employee.getId(), (employee.getFirstName()) + " " + employee.getLastName());
    }

    private EmployeeResponseDTO convertToEmployeeResponseDTO(EmployeeEntity employee) {
        return new EmployeeResponseDTO(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getEmailAddress(), employee.getPhoneNumber());
    }
}
