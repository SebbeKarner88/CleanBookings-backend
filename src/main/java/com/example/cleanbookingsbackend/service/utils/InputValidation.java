package com.example.cleanbookingsbackend.service.utils;

import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataField.EMPLOYEE_ID;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataType.STRING;
import static io.micrometer.common.util.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
public class InputValidation {
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final JobRepository jobRepository;
    private final PaymentRepository paymentRepository;

    private static final String UNAUTHORIZED_CALL_MESSAGE = "You are not authorized to perform this action.";

    public enum DataField {
        CUSTOMER_ID("Customer id"),
        EMPLOYEE_ID("Employee id"),
        JOB_ID("Job id"),
        PAYMENT_ID("Payment id"),
        DATE("Date");

        private final String fieldName;

        DataField(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }

    public enum DataType {
        STRING,
        NUMERIC,
    }

    public static void validateInputDataField(DataField field, DataType dataType, String value)
            throws IllegalArgumentException {
        switch (dataType) {
            case STRING -> {
                if (isBlank(value))
                    throw new IllegalArgumentException(field.getFieldName() + " is required.");
            }
            case NUMERIC -> {
                if (value == null)
                    throw new IllegalArgumentException(field.getFieldName() + " is required.");
            }
        }
    }

    public static JobType validateJobType(String requestedType) {
        JobType type;
        switch (requestedType.toUpperCase()) {
            case "BASIC" -> type = JobType.BASIC_CLEANING;
            case "TOPP" -> type = JobType.TOPP_CLEANING;
            case "DIAMOND" -> type = JobType.DIAMOND_CLEANING;
            case "WINDOW" -> type = JobType.WINDOW_CLEANING;
            default -> throw new IllegalArgumentException("Invalid job type");
        }
        return type;
    }

    public PrivateCustomerEntity validateCustomerId(String id) throws CustomerNotFoundException {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("There is no customer with id: " + id));
    }

    public EmployeeEntity validateEmployeeId(String id) throws EmployeeNotFoundException {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("There is no employee with id: " + id));
    }

    public JobEntity validateJobId(String id) throws JobNotFoundException {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException("There is no job with id: " + id));
    }

    public PaymentEntity validatePaymentId(String id) throws PaymentNotFoundException {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("There is no payment with id: " + id));
    }

    public boolean isAdmin(String id)
            throws EmployeeNotFoundException, UnauthorizedCallException {
        validateInputDataField(EMPLOYEE_ID, STRING, id);
        EmployeeEntity employee = validateEmployeeId(id);
        if (!employee.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        return true;
    }

    public boolean isValidEmailAddress(String email) {
        return email.length() >= 5 && email.contains("@");
    }

    public boolean isValidPassword(String password) {
        return password.length() >= 3;
    }
}
