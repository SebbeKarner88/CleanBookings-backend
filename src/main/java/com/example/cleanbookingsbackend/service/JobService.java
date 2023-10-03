package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.*;
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
import com.example.cleanbookingsbackend.service.utils.InputValidation;
import com.example.cleanbookingsbackend.service.utils.MailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataField.*;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataType.*;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateInputDataField;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateJobType;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final MailSenderService mailSender;
    private final InputValidation input;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String INVALID_ROLE_MESSAGE = "Invalid role. An admin cannot be assigned to a cleaning job.";
    private static final String UNAUTHORIZED_CALL_MESSAGE = "You are not authorized to perform this action.";
    private static final String CANCEL_COMPLETED_JOB_MESSAGE = "You may not cancel a completed job.";
    private static final String USER_ID_REQUIRED_MESSAGE = "A customer or admin id is required.";

    public CreateJobResponse createJobRequest(CreateJobRequest request)
            throws IllegalArgumentException, CustomerNotFoundException, ParseException {
        validateJobRequestInputData(request);
        CustomerEntity customer = input.validateCustomerId(request.customerId());
        JobType type = validateJobType(request.type());
        Date date = DATE_FORMAT.parse(request.date());
        if (jobRepository.findByBookedDateAndType(date, type).isPresent())
            throw new IllegalArgumentException("There is already a job of type " + type + " requested on " + date);
        JobEntity requestedJob = new JobEntity(customer, type, date, request.message());
        jobRepository.save(requestedJob);
        mailSender.sendEmailConfirmationBookedJob(requestedJob);

        return convertToCreateJobResponseDTO(requestedJob);
    }

    public boolean cancelJobRequest(JobUserRequest request)
            throws IllegalArgumentException, JobNotFoundException, NotFoundException, UnauthorizedCallException {
        validateCancelJobInputData(request);
        authorizedCancellation(request);
        mailSender.sendEmailConfirmationCanceledJob(jobRepository.findById(request.jobId()).get());
        return true;
    }

    public void assignCleanerRequest(AssignCleanerRequest request)
            throws EmployeeNotFoundException, JobNotFoundException, IllegalArgumentException {
        if (isValidAssignRequest(request))
            assignCleanersAndSendEmailConfirmation(request.jobId(), request.cleanerIds());
    }

    private void assignCleanersAndSendEmailConfirmation(String jobId, List<String> cleanerIds) throws JobNotFoundException {
        JobEntity job = input.validateJobId(jobId);
        List<EmployeeEntity> assignedCleaners = job.getEmployee();

        // Filter out cleaners that are already assigned to the requested job
        List<String> newCleanerIds = cleanerIds
                .stream()
                .filter(id -> assignedCleaners
                        .stream()
                        .noneMatch(assignedCleaner -> assignedCleaner.getId().equals(id)))
                .toList();

        // Send email notifications to the newly assigned cleaners
        newCleanerIds.forEach(id -> {
            EmployeeEntity cleaner = input.validateEmployeeId(id);
            mailSender.sendEmailConfirmationOnAssignedJob(cleaner, job);
        });

        // Combine the old and new cleaners
        List<EmployeeEntity> updatedListOfCleaners = new ArrayList<>(assignedCleaners);
        updatedListOfCleaners.addAll(
                newCleanerIds
                        .stream()
                        .map(input::validateEmployeeId)
                        .toList()
        );

        // Update the JobEntity with the combined list of cleaners
        job.setEmployee(updatedListOfCleaners);

        // Set the status to ASSIGNED if this is the first time
        if (job.getStatus() == JobStatus.OPEN)
            job.setStatus(JobStatus.ASSIGNED);

        jobRepository.save(job);
    }

    public void executedCleaningRequest(JobUserRequest request)
            throws IllegalArgumentException, EmployeeNotFoundException, JobNotFoundException {
        validateExecutedCleaningInputData(request);
        reportExecutedCleaning(request);
    }

    public void approveDeclineCleaningRequest(JobApproveRequest request)
            throws IllegalArgumentException, EmployeeNotFoundException, JobNotFoundException, CustomerNotFoundException, UnauthorizedCallException {
        validateApprovedCleaningInputData(request);
        approveDeclineCleaning(request);
    }

    private void approveDeclineCleaning(JobApproveRequest request)
            throws JobNotFoundException {
        JobEntity job = input.validateJobId(request.jobId());

        job.setMessage(request.message()); // Setting a message to let cleaner/admin know what needs to be supplemented for an approval etc.

        if (request.isApproved()) {
            job.setStatus(JobStatus.APPROVED);
            // WIP mailSender.sendEmailConfirmationApprovedJob(request);
            jobRepository.save(job);
        } else {
            job.setStatus(JobStatus.NOT_APPROVED);
            // WIP mailSender.sendEmailConfirmationFailedJob(request);
            jobRepository.save(job);
        }
    }

    public List<JobEntity> getBookedCleaningsForCustomer(String customerId) {
        return jobRepository.findByCustomer_Id(customerId);
    }

    private void reportExecutedCleaning(JobUserRequest request)
            throws JobNotFoundException {
        JobEntity job = input.validateJobId(request.jobId());
        EmployeeEntity cleaner = input.validateEmployeeId(request.userId());
        if (!job.getEmployee().contains(cleaner))
            throw new IllegalArgumentException("You can only report jobs you are assigned to.");
        job.setStatus(JobStatus.WAITING_FOR_APPROVAL);
        jobRepository.save(job);
        mailSender.sendEmailConfirmationExecutedJob(job, cleaner);
    }

    private void authorizedCancellation(JobUserRequest request)
            throws UnauthorizedCallException, NotFoundException, JobNotFoundException {
        Optional<CustomerEntity> customerOptional = customerRepository.findById(request.userId());
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findById(request.userId());

        if (customerOptional.isEmpty() && employeeOptional.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.userId());
        }

        JobEntity job = input.validateJobId(request.jobId());

        if (customerOptional.isPresent()) {
            CustomerEntity customer = customerOptional.get();
            checkCustomerAuthorization(customer, job);
        } else if (employeeOptional.get().getRole().equals(Role.CLEANER)) {
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE +
                    "\nOnly " + Role.ADMIN + " are allowed to cancel a booked cleaning.");
        }
        jobRepository.deleteById(request.jobId());
    }

    private void validateApprovedCleaningInputData(JobApproveRequest request)
            throws JobNotFoundException, UnauthorizedCallException, CustomerNotFoundException {
        validateInputDataField(JOB_ID, STRING, request.jobId());
        validateInputDataField(CUSTOMER_ID, STRING, request.customerId());
        JobEntity job = input.validateJobId(request.jobId());
        CustomerEntity customer = input.validateCustomerId(request.customerId());
        if (!Objects.equals(customer.getId(), job.getCustomer().getId()))
            throw new UnauthorizedCallException("Only the customer who booked the cleaning can approve or deny.");
    }

    private void validateExecutedCleaningInputData(JobUserRequest request)
            throws JobNotFoundException, EmployeeNotFoundException, IllegalArgumentException {
        validateInputDataField(EMPLOYEE_ID, STRING, request.userId());
        validateInputDataField(JOB_ID, STRING, request.jobId());
        input.validateEmployeeId(request.userId()); // JUST FOR VALIDATION.
        JobEntity job = input.validateJobId(request.jobId());
        if(job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.CLOSED)
            throw new IllegalArgumentException("A unassigned or finished job cant be marked as executed.");
    }

    private boolean isValidAssignRequest(AssignCleanerRequest request)
            throws EmployeeNotFoundException, JobNotFoundException, IllegalArgumentException {
        validateInputDataField(EMPLOYEE_ID, STRING, request.adminId());
        EmployeeEntity admin = input.validateEmployeeId(request.adminId());
        if (!admin.getRole().equals(Role.ADMIN))
            throw new IllegalArgumentException("Only an admin can assign a cleaner to a job.");

        validateInputDataField(JOB_ID, STRING, request.jobId());
        JobEntity job = input.validateJobId(request.jobId());
        if (job.getStatus().equals(JobStatus.CLOSED)) {
            throw new IllegalArgumentException("The job with id " + request.jobId() + " has already been executed and closed.");
        }

        for (String id : request.cleanerIds()) {
            validateInputDataField(EMPLOYEE_ID, STRING, id);
            EmployeeEntity cleaner = input.validateEmployeeId(id);
            if (!cleaner.getRole().equals(Role.CLEANER)) {
                throw new IllegalArgumentException(INVALID_ROLE_MESSAGE);
            }
        }
        return true;
    }

    private void validateCancelJobInputData(JobUserRequest request) {
        if (request.userId().isBlank())
            throw new IllegalArgumentException(USER_ID_REQUIRED_MESSAGE);
        validateInputDataField(JOB_ID, STRING, request.jobId());
    }

    private void validateJobRequestInputData(CreateJobRequest request) {
        validateInputDataField(CUSTOMER_ID, STRING, request.customerId());
        validateInputDataField(DATE, STRING, request.date());
    }

    private static void checkCustomerAuthorization(CustomerEntity customer, JobEntity job)
            throws UnauthorizedCallException {
        if (job.getCustomer() != customer) {
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE +
                    "\nThe customer who booked the job is the only one allowed to cancel this booked cleaning.");
        }
        if (job.getStatus() != JobStatus.OPEN && job.getStatus() != JobStatus.ASSIGNED) {
            throw new UnauthorizedCallException(CANCEL_COMPLETED_JOB_MESSAGE);
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
                .date(DATE_FORMAT.format(job.getBookedDate()))
                .customer(customerDto)
                .message(job.getMessage())
                .build();
    }
}
