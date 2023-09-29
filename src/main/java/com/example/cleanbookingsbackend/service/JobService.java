package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.CancelJobRequest;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.dto.CreateJobRequest;
import com.example.cleanbookingsbackend.dto.CreateJobResponse;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.CustomerNotFoundException;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.exception.NotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
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
import java.util.Date;
import java.util.List;

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
        if (jobRepository.findJobEntityByBookedDateAndType(date, type).isPresent())
            throw new IllegalArgumentException("There is already a job of type " + type + " requested on " + date);

        JobEntity requestedJob = new JobEntity(customer, type, date, request.message());
        jobRepository.save(requestedJob);

        sendEmailConfirmationBookedJob(requestedJob);

        return toDTO(requestedJob);
    }

    public boolean cancelJobRequest(CancelJobRequest request)
            throws IllegalArgumentException, JobNotFoundException, NotFoundException, UnauthorizedCallException {

        validateCancelJobInputData(request);

        if (jobRepository.findById(request.jobId()).isEmpty())
            throw new JobNotFoundException("There is no job registered with id: " + request.jobId());

        authorizedCancellation(request);

        return true;
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

    private void authorizedCancellation(CancelJobRequest request) throws UnauthorizedCallException, NotFoundException {

        JobEntity job;

        try {
            job = jobRepository.findById(request.jobId()).get();
        } catch (Exception e) {
            throw new NotFoundException("There is no job with id: " + request.jobId());
        }

        if (customerRepository.findById(request.userId()).isPresent()) {

            if (job.getCustomer() == customerRepository.findById(request.userId()).get()) {

                if (job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.ASSIGNED) {
                    jobRepository.deleteById(request.jobId());
                } else {
                    throw new UnauthorizedCallException("You may not cancel a completed job.");
                }
            } else {
                throw new UnauthorizedCallException("You are not authorized to perform this action." +
                        "\nThe customer who booked the job is the only one allowed to cancel this booked cleaning.");
            }

        } else if (employeeRepository.findById(request.userId()).isPresent()) {

            if (employeeRepository.findById(request.userId()).get().getRole() == Role.CLEANER) {
                throw new UnauthorizedCallException("You are not authorized to perform this action." +
                        "\nOnly " + Role.ADMIN + " are allowed to cancel a booked cleaning.");
            } else {
                jobRepository.deleteById(request.jobId());
            }

        } else {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.userId());
        }
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

    private void sendEmailConfirmationCanceledJob(JobEntity requestedJob) {
        //WIP
    }

    private CreateJobResponse toDTO(JobEntity job) {
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

    public List<JobEntity> getBookedCleaningsForCustomer(String customerId) throws CustomerNotFoundException {
        CustomerEntity customer = validateCustomerId(customerId);
        return jobRepository.findByCustomerAndStatusNot(customer, JobStatus.CLOSED);
    }

}
