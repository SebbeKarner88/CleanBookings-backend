package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.dto.CreateJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobResponse;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public CreateJobResponse createJobRequest(CreateJobRequest request)
            throws IllegalArgumentException, CustomerNotFoundException, ParseException {
        validateJobRequestInputData(request);
        CustomerEntity customer = validateCustomerId(request.customerId());
        JobType type = validateJobType(request.type());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(request.date());
        if (jobRepository.findJobEntityByBookedDateAndType(date, type).isPresent())
            throw new IllegalArgumentException("There is already a job of type " + type + " requested on " + date);

        JobEntity requestedJob = new JobEntity(customer, type, date);
        jobRepository.save(requestedJob);
        return toDTO(requestedJob);
    }

//    private List<EmployeeEntity> validateEmployeeIds(List<String> ids) {
//        List<EmployeeEntity> employees;
//        for (String id : ids) {
//            if (employeeRepository.findById(id).isEmpty())
//                throw new EmployeeNotFoundException("There is no employee with id: " + id);
//        }
//        employees = employeeRepository.findAllById(ids);
//        return employees;
//    }

    private CustomerEntity validateCustomerId(String id) {
        CustomerEntity customer;
        if (customerRepository.findById(id).isEmpty())
            throw new CustomerNotFoundException("There is no customer with id: " + id);
        customer = customerRepository.findById(id).get();
        return customer;
    }

    private JobType validateJobType(String requestedType) {
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

    private void validateJobRequestInputData(CreateJobRequest request) {
        if (request.customerId().isBlank())
            throw new IllegalArgumentException("Customer id is required");

        if (request.type().isBlank())
            throw new IllegalArgumentException("Job type is required.");

        if (request.date().isBlank())
            throw new IllegalArgumentException("Date is required.");

//        for (String id : request.employeeIds()) {
//            if (id.isBlank())
//                throw new IllegalArgumentException("Employee id is required");
//        }
    }

    private CreateJobResponse toDTO(JobEntity job) {
        CustomerEntity customer = job.getCustomer();
        CreateJobResponse.Adress adress = new CreateJobResponse.Adress(
                customer.getStreetAddress(),
                customer.getPostalCode(),
                customer.getCity()
        );

        return CreateJobResponse
                .builder()
                .jobId(job.getId())
                .jobType(job.getType())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(job.getBookedDate()))
                .customer(customer.getFirstName() + " " + customer.getLastName())
                .phoneNumber(customer.getPhoneNumber())
                .emailAdress(customer.getEmailAddress())
                .adress(adress)
                .build();
    }
}
