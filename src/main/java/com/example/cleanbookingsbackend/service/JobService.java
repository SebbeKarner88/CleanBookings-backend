package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.*;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.model.PrivateCustomerEntity;
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
import static com.example.cleanbookingsbackend.service.utils.InputValidation.DataType.STRING;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateInputDataField;
import static com.example.cleanbookingsbackend.service.utils.InputValidation.validateJobType;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentService paymentService;
    private final MailSenderService mailSender;
    private final InputValidation input;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final String INVALID_ROLE_MESSAGE = "Invalid role. An admin cannot be assigned to a cleaning job.";
    private static final String UNAUTHORIZED_CALL_MESSAGE = "You are not authorized to perform this action.";
    private static final String CANCEL_COMPLETED_JOB_MESSAGE = "You may not cancel a completed job.";
    private static final String USER_ID_REQUIRED_MESSAGE = "A customer or admin id is required.";

    public CreateJobResponse createJobRequest(CreateJobRequest request)
            throws IllegalArgumentException, CustomerNotFoundException, ParseException {
        JobEntity requestedJob = new JobEntity();
        if (isValidCreateJobRequest(request)) {
            PrivateCustomerEntity customer = input.validateCustomerId(request.customerId());
            JobType type = validateJobType(request.type());
            Date date = DATE_FORMAT.parse(request.date());
            if (jobRepository.findByBookedDateAndType(date, type).isPresent())
                throw new IllegalArgumentException("There is already a job of type " + type + " requested on " + date);
            requestedJob = new JobEntity(customer, type, date, request.message());
            jobRepository.save(requestedJob);
            mailSender.sendEmailConfirmationBookedJob(requestedJob);
        }
        return convertToCreateJobResponseDTO(requestedJob);

    }

    public boolean cancelJobRequest(JobUserRequest request)
            throws IllegalArgumentException, JobNotFoundException, NotFoundException, UnauthorizedCallException {
        if (isValidCancelJobRequest(request)) {
            authorizedCancellation(request);
            mailSender.sendEmailConfirmationCanceledJob(jobRepository.findById(request.jobId()).get());
        }
        return true;
    }

    public void assignCleanerRequest(AssignCleanerRequest request)
            throws EmployeeNotFoundException, JobNotFoundException, IllegalArgumentException {
        if (isValidAssignCleanerRequest(request))
            assignCleanersAndSendEmailConfirmation(request.jobId(), request.cleanerIds());
    }

    public void executedCleaningRequest(JobUserRequest request)
            throws IllegalArgumentException, EmployeeNotFoundException, JobNotFoundException {
        if (isValidExecutedCleaningRequest(request))
            reportExecutedCleaning(request);
    }

    public void approveDeclineCleaningRequest(JobApproveRequest request)
            throws IllegalArgumentException, EmployeeNotFoundException, JobNotFoundException, CustomerNotFoundException, UnauthorizedCallException {
        if (isValidApproveDeclineCleaningRequest(request))
            updateJobStatusAndMessage(request);
    }

    public void reissueFailedCleaningRequest(JobUserRequest request)
            throws JobNotFoundException, EmployeeNotFoundException, UnauthorizedCallException {
        isValidReissueFailedCleaningRequest(request);
        reissueCleaning(request);
    }

    private void reissueCleaning(JobUserRequest request) throws JobNotFoundException {
        JobEntity reIssuedJob = input.validateJobId(request.jobId());
        reIssuedJob.setStatus(JobStatus.ASSIGNED);
        jobRepository.save(reIssuedJob);
        mailSender.sendEmailConfirmationReissuedJob(reIssuedJob);
    }

    public List<JobResponseDTO> getAllJobs(String id) throws EmployeeNotFoundException, UnauthorizedCallException {
        List<JobResponseDTO> jobs = new ArrayList<>();
        if (isValidGetAllJobsRequest(id))
            jobs = jobRepository
                    .findAll()
                    .stream()
                    .map(this::convertToJobResponseDTO)
                    .toList();
        return jobs;
    }

    private boolean isValidGetAllJobsRequest(String id) throws UnauthorizedCallException {
        validateInputDataField(EMPLOYEE_ID, STRING, id);
        EmployeeEntity employee = input.validateEmployeeId(id);
        if (!employee.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        return true;
    }

    public List<JobResponseDTO> getAllJobsCleaner(String id) throws EmployeeNotFoundException, UnauthorizedCallException {
        List<JobResponseDTO> jobs = new ArrayList<>();
        if (isValidGetAllJobsCleanerRequest(id))
            jobs = jobRepository
                    .findAllByEmployeeId(id)
                    .stream()
                    .map(this::convertToJobResponseDTO)
                    .toList();
        return jobs;
    }

    public List<JobResponseDTO> getAllJobsCustomer(String id) throws EmployeeNotFoundException, UnauthorizedCallException {
        List<JobResponseDTO> jobs = new ArrayList<>();
        if (isValidGetAllJobsCustomerRequest(id))
            jobs = jobRepository
                    .findAllByCustomerId(id)
                    .stream()
                    .map(this::convertToJobResponseDTO)
                    .toList();
        return jobs;
    }

    private boolean isValidGetAllJobsCleanerRequest(String id) throws UnauthorizedCallException {
        validateInputDataField(EMPLOYEE_ID, STRING, id);
        EmployeeEntity employee = input.validateEmployeeId(id);
        if (!employee.getRole().equals(Role.CLEANER))
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        return true;
    }

    private boolean isValidGetAllJobsCustomerRequest(String id) throws UnauthorizedCallException {
        validateInputDataField(CUSTOMER_ID, STRING, id);
        PrivateCustomerEntity customer = input.validateCustomerId(id);
//        if (!employee.getRole().equals(Role.CLEANER))
//            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        return true;
    }

//    private boolean isValidGetAllJobsCustomerRequest(String id) throws UnauthorizedCallException {
//        validateInputDataField(CUSTOMER_ID, STRING, id);
//        PrivateCustomerEntity customer = input.validateCustomerId(id);
////        if (!employee.getRole().equals(Role.CLEANER))
////            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
//        return true;
//    }

    public List<JobEntity> getBookedCleaningsForCustomer(String customerId) {
        return jobRepository.findByCustomer_Id(customerId);
    }

    public List<JobEntity> getBookingHistoryForCustomer(String customerId) {
        return jobRepository.findJobsByCustomerIdAndStatusClosed(customerId);
    }

    public List<JobEntity> getCleaningsByStatusAndCustomerId(String customerId, JobStatus status) {
        return jobRepository.findByCustomerIdAndStatus(customerId, status);
    }

    public List<JobEntity> getAllCleaningsForCustomer(String customerId) {
        return jobRepository.findAllByCustomerId(customerId);
    }


    public List<JobEntity> getAllCleaningsForAllCleaners() {
        return jobRepository.findAll(); // Retrieve all job entities
    }

    public List<JobEntity> getCleaningsByStatusAndEmployeeId(String employeeId, JobStatus status) {
        return jobRepository.findByEmployeeIdAndStatus(employeeId, status);
    }

    public List<JobEntity> getCleaningsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public List<JobEntity> getAllCleaningsForCleaner(String employeeId) {
        return jobRepository.findByEmployeeId(employeeId);
    }

    public void deleteJob(String employeeId, String jobId)
            throws EmployeeNotFoundException, UnauthorizedCallException, JobNotFoundException {
        if (isValidDeleteRequest(employeeId, jobId)) {
            jobRepository.deleteById(jobId);
        }
    }

    private void updateJobStatusAndMessage(JobApproveRequest request)
            throws JobNotFoundException {
        JobEntity job = input.validateJobId(request.jobId());

        if (!request.message().isBlank()) {
            String addedMessage = "\n\nCustomer message " + new SimpleDateFormat("yyyy-MM-dd hh:mm")
                    .format(System.currentTimeMillis()) + "\nMessage: " + request.message() + ".";
            job.setMessage(job.getMessage().concat(addedMessage));
        }

        if (request.isApproved()) {
            job.setStatus(JobStatus.APPROVED);
            paymentService.createInvoiceOnJob(job);
            mailSender.sendEmailConfirmationApprovedJob(job);
            jobRepository.save(job);
        } else {
            job.setStatus(JobStatus.NOT_APPROVED);
            mailSender.sendEmailConfirmationFailedJob(job);
            jobRepository.save(job);
        }
    }

    private void assignCleanersAndSendEmailConfirmation(String jobId, List<String> cleanerIds)
            throws JobNotFoundException {
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

        // Set the status to ASSIGNED if this is the first time or the job was rejected before
        if (job.getStatus().equals(JobStatus.OPEN) || job.getStatus().equals(JobStatus.NOT_APPROVED))
            job.setStatus(JobStatus.ASSIGNED);

        jobRepository.save(job);
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
        Optional<PrivateCustomerEntity> customerOptional = customerRepository.findById(request.userId());
        Optional<EmployeeEntity> employeeOptional = employeeRepository.findById(request.userId());

        if (customerOptional.isEmpty() && employeeOptional.isEmpty()) {
            throw new NotFoundException("No Customer or Administrator exists by id: " + request.userId());
        }

        JobEntity job = input.validateJobId(request.jobId());

        if (customerOptional.isPresent()) {
            PrivateCustomerEntity customer = customerOptional.get();
            checkCustomerAuthorization(customer, job);
        } else if (employeeOptional.get().getRole().equals(Role.CLEANER)) {
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE +
                    "\nOnly " + Role.ADMIN + " are allowed to cancel a booked cleaning.");
        }
        jobRepository.deleteById(request.jobId());
    }

    private void isValidReissueFailedCleaningRequest(JobUserRequest request)
            throws JobNotFoundException, EmployeeNotFoundException, UnauthorizedCallException {
        validateInputDataField(EMPLOYEE_ID, STRING, request.userId());
        validateInputDataField(JOB_ID, STRING, request.jobId());
        EmployeeEntity admin = input.validateEmployeeId(request.userId());
        if (admin.getRole() != Role.ADMIN)
            throw new UnauthorizedCallException("Only an administrator can re-issue failed jobs.");
        JobEntity job = input.validateJobId(request.jobId());
        if (job.getStatus() != JobStatus.NOT_APPROVED)
            throw new UnauthorizedCallException("Only NOT_APPROVED jobs can be re-issued.");
    }

    private boolean isValidApproveDeclineCleaningRequest(JobApproveRequest request)
            throws JobNotFoundException, UnauthorizedCallException, CustomerNotFoundException {
        validateInputDataField(JOB_ID, STRING, request.jobId());
        validateInputDataField(CUSTOMER_ID, STRING, request.customerId());
        JobEntity job = input.validateJobId(request.jobId());
        PrivateCustomerEntity customer = input.validateCustomerId(request.customerId());
        if (!Objects.equals(customer.getId(), job.getCustomer().getId()))
            throw new UnauthorizedCallException("Only the customer who booked the cleaning can approve or deny.");
        return true;
    }

    private boolean isValidExecutedCleaningRequest(JobUserRequest request)
            throws JobNotFoundException, EmployeeNotFoundException, IllegalArgumentException {
        validateInputDataField(EMPLOYEE_ID, STRING, request.userId());
        validateInputDataField(JOB_ID, STRING, request.jobId());
        input.validateEmployeeId(request.userId()); // JUST FOR VALIDATION.
        JobEntity job = input.validateJobId(request.jobId());
        if (job.getStatus() == JobStatus.OPEN || job.getStatus() == JobStatus.CLOSED)
            throw new IllegalArgumentException("A unassigned or finished job cant be marked as executed.");
        return true;
    }

    private boolean isValidAssignCleanerRequest(AssignCleanerRequest request)
            throws EmployeeNotFoundException, JobNotFoundException, IllegalArgumentException {
        validateInputDataField(EMPLOYEE_ID, STRING, request.adminId());
        EmployeeEntity admin = input.validateEmployeeId(request.adminId());
        if (!admin.getRole().equals(Role.ADMIN))
            throw new IllegalArgumentException("Only an admin can assign a cleaner to a job.");

        validateInputDataField(JOB_ID, STRING, request.jobId());
        JobEntity job = input.validateJobId(request.jobId());
        if (job.getStatus().equals(JobStatus.CLOSED)) {
            throw new IllegalArgumentException("The job with id " + request.jobId()
                    + " has already been executed and closed.");
        }

        if (request.cleanerIds() == null)
            throw new IllegalArgumentException("A list of employee ids is required.");

        for (String id : request.cleanerIds()) {
            validateInputDataField(EMPLOYEE_ID, STRING, id);
            EmployeeEntity cleaner = input.validateEmployeeId(id);
            if (!cleaner.getRole().equals(Role.CLEANER)) {
                throw new IllegalArgumentException(INVALID_ROLE_MESSAGE);
            }
        }
        return true;
    }

    private boolean isValidCancelJobRequest(JobUserRequest request) {
        if (request.userId().isBlank())
            throw new IllegalArgumentException(USER_ID_REQUIRED_MESSAGE);
        validateInputDataField(JOB_ID, STRING, request.jobId());
        return true;
    }

    private boolean isValidCreateJobRequest(CreateJobRequest request) {
        validateInputDataField(CUSTOMER_ID, STRING, request.customerId());
        validateInputDataField(DATE, STRING, request.date());
        return true;
    }

    private boolean isValidDeleteRequest(String employeeId, String jobId) throws JobNotFoundException, UnauthorizedCallException {
        validateInputDataField(EMPLOYEE_ID, STRING, employeeId);
        validateInputDataField(JOB_ID, STRING, jobId);
        EmployeeEntity admin = input.validateEmployeeId(employeeId);
        JobEntity job = input.validateJobId(jobId);
        if (!admin.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException(UNAUTHORIZED_CALL_MESSAGE);
        if (job.getStatus().equals(JobStatus.APPROVED) || job.getStatus().equals(JobStatus.CLOSED))
            throw new IllegalArgumentException("You can't remove a job that has already been approved or closed.");
        return true;
    }

    private static void checkCustomerAuthorization(PrivateCustomerEntity customer, JobEntity job)
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
        PrivateCustomerEntity customer = job.getCustomer();
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
                .date(new SimpleDateFormat("yyyy-MM-dd hh:mm").format(job.getBookedDate()))
                .customer(customerDto)
                .message(job.getMessage())
                .build();
    }


    // Helper method to convert JobEntity to JobDto
    private JobDto convertToJobDto(JobEntity jobEntity) {
        JobDto jobDto = new JobDto();
        jobDto.setId(jobEntity.getId());
        // Populate other fields in JobDto based on jobEntity
        return jobDto;
    }

    private JobResponseDTO convertToJobResponseDTO(JobEntity job) {
        return new JobResponseDTO(
                job.getId(),
                job.getType().toString(),
                job.getStatus().toString(),
                job.getMessage(),
                job.getCustomer().getId(),
                job.getEmployee().stream().map(employee -> employee.getFirstName().concat(" ").concat(employee.getLastName())).toList()
        );
    }
}
