package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.AssignCleanerRequest;
import com.example.cleanbookingsbackend.dto.CancelJobRequest;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.dto.CreateJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobResponse;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final JavaMailSender mailSender;


    public CreateJobResponse createJobRequest(CreateJobRequest request)
            throws IllegalArgumentException, CustomerNotFoundException, ParseException {

        validateJobRequestInputData(request);
        CustomerEntity customer = validateCustomerId(request.customerId());
        JobType type = validateJobType(request.type());
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(request.date());
        if (jobRepository.findByBookedDateAndType(date, type).isPresent())
            throw new IllegalArgumentException("There is already a job of type " + type + " requested on " + date);

        JobEntity requestedJob = new JobEntity(customer, type, date, request.message());
        jobRepository.save(requestedJob);

        sendEmailConfirmationBookedJob(requestedJob);

        return convertToCreateJobResponseDTO(requestedJob);
    }

    public boolean cancelJobRequest(CancelJobRequest request)
            throws IllegalArgumentException, JobNotFoundException, NotFoundException, UnauthorizedCallException {
        validateCancelJobInputData(request);
        authorizedCancellation(request);
        sendEmailConfirmationCanceledJob(jobRepository.findById(request.jobId()).get());
        return true;
    }

    public void assignCleanerRequest(AssignCleanerRequest request) throws NotFoundException, ValidationException, UnauthorizedCallException {
        validateAssignCleanerInputData(request);
        assignCleaners(request);
    }


//    TODO: Keeping this for future use. Will be needed in PUT-request to update a job when assigned.
    //    private List<EmployeeEntity> validateEmployeeIds(List<String> ids) {
//        List<EmployeeEntity> employees;
//        for (String id : ids) {
//            if (employeeRepository.findById(id).isEmpty())
//                throw new EmployeeNotFoundException("There is no employee with id: " + id);
//        }
//        employees = employeeRepository.findAllById(ids);
//        return employees;
//    }

    private void assignCleaners(AssignCleanerRequest request) {

        JobEntity job = jobRepository.findById(request.jobId()).get();
        List<EmployeeEntity> cleaners = job.getEmployee();

        for (String id : request.cleanerId()) {
            EmployeeEntity cleaner = employeeRepository.findById(id).get();
            if (cleaners.contains(cleaner))
                continue;
            cleaners.add(cleaner);
            cleanerEmailConfirmationOnAssignedJob(cleaner, job);
        }
        job.setEmployee(cleaners);
        if (job.getStatus() == JobStatus.OPEN)
            job.setStatus(JobStatus.ASSIGNED);
        jobRepository.save(job);
    }

    private void authorizedCancellation(CancelJobRequest request) throws UnauthorizedCallException, NotFoundException {
        Optional<CustomerEntity> customerOptional = customerRepository.findById(request.userId());
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findById(request.userId());

        if (customerOptional.isEmpty() && employeeOptional.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.userId());
        }

        JobEntity job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("There is no job with id: " + request.jobId()));

        if (customerOptional.isPresent()) {
            CustomerEntity customer = customerOptional.get();
            if (job.getCustomer() != customer) {
                throw new UnauthorizedCallException("You are not authorized to perform this action." +
                        "\nThe customer who booked the job is the only one allowed to cancel this booked cleaning.");
            }
            if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.ASSIGNED) {
                throw new UnauthorizedCallException("You may not cancel a completed job.");
            }
        } else if (employeeOptional.isPresent()) {
            EmployeeEntity employee = employeeOptional.get();
            if (employee.getRole() == Role.CLEANER) {
                throw new UnauthorizedCallException("You are not authorized to perform this action." +
                        "\nOnly " + Role.ADMIN + " are allowed to cancel a booked cleaning.");
            }
        }
        jobRepository.deleteById(request.jobId());
    }

    private void validateAssignCleanerInputData(AssignCleanerRequest request) throws NotFoundException, ValidationException, UnauthorizedCallException {
        if (request.adminId().isBlank())
            throw new IllegalArgumentException("A admin id is required");
        if (!employeeRepository.existsById(request.adminId()))
            throw new NotFoundException("No admin found with id: " + request.adminId());
        if (employeeRepository.findById(request.adminId()).get().getRole() != Role.ADMIN)
            throw new ValidationException("Only a admin can assign a cleaner to a job.");

        if (request.cleanerId().isEmpty())
            throw new IllegalArgumentException("At least one cleaner id is required");
        for (String id : request.cleanerId()) {
            if (!employeeRepository.existsById(id))
                throw new NotFoundException("No cleaner found with id: " + id);
            if (employeeRepository.findById(id).get().getRole() != Role.CLEANER)
                throw new ValidationException("A admin can not be assigned to a job");
        }

        if (request.jobId().isBlank())
            throw new IllegalArgumentException("A job id is required.");
        if (!jobRepository.existsById(request.jobId()))
            throw new NotFoundException("No job found with id: " + request.jobId());
        if (jobRepository.findById(request.jobId()).get().getStatus() == JobStatus.CLOSED)
            throw new UnauthorizedCallException("This job is finished.");
    }

    private void validateCancelJobInputData(CancelJobRequest request) {
        if (request.userId().isBlank())
            throw new IllegalArgumentException("A customer or admin id is required");
        if (request.jobId().isBlank())
            throw new IllegalArgumentException("A job id is required.");
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

    private void sendEmailConfirmationBookedJob(JobEntity requestedJob) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(requestedJob.getCustomer().getEmailAddress());
        msg.setSubject("Din bokningsbekräftelse");
        msg.setText("Hej " + requestedJob.getCustomer().getFirstName() + "! Din bokning av " + requestedJob.getType() + " på " + requestedJob.getBookedDate() + " har bekräftats.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    private void sendEmailConfirmationCanceledJob(JobEntity requestedCancel) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(requestedCancel.getCustomer().getEmailAddress());
        msg.setSubject("Avbokad städning");
        msg.setText("Hej " + requestedCancel.getCustomer().getFirstName() + "! Er bokning av " + requestedCancel.getType() + " på " + requestedCancel.getBookedDate() + " är nu avbokad. /n" +
                "Varmt välkommen åter!");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    private void cleanerEmailConfirmationOnAssignedJob(EmployeeEntity cleaner, JobEntity job) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("order.cleanbookings@gmail.com");
        msg.setTo(cleaner.getEmailAddress());
        msg.setSubject("Nytt städjobb för " + cleaner.getFirstName() + "!");
        msg.setText("Hej " + cleaner.getFirstName() + "! /n/nDu har fått ett nytt städjobb inbokat: "
                + job.getBookedDate() + ", " + job.getType() + "Meddelande: " + job.getMessage() + "/n/nFör mer information logga in på CleanBookings.");
        try {
            mailSender.send(msg);
        } catch (MailException exception) {
            System.out.println("Email couldn't be sent: " + exception.getMessage());
        }
    }

    private CreateJobResponse convertToCreateJobResponseDTO(JobEntity job) {
        CustomerEntity customer = job.getCustomer();
        CreateJobResponse.Adress adressDto = new CreateJobResponse.Adress(
                customer.getStreetAddress(),
                customer.getPostalCode(),
                customer.getCity()
        );
        CreateJobResponse.Customer customerDto = new CreateJobResponse.Customer(
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getEmailAddress(),
                adressDto
        );

        return CreateJobResponse
                .builder()
                .jobId(job.getId())
                .jobType(job.getType())
                .date(new SimpleDateFormat("yyyy-MM-dd").format(job.getBookedDate()))
                .customer(customerDto)
                .message(job.getMessage())
                .build();
    }


    public List<JobEntity> getBookedCleaningsForCustomer(String customerId) {

        return jobRepository.findByCustomer_Id(customerId);
    }

}
