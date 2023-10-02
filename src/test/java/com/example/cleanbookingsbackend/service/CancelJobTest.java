package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.CancelJobRequest;
import com.example.cleanbookingsbackend.enums.CustomerType;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.exception.NotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.CustomerRepository;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.service.utils.MailSenderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class CancelJobTest {

    @InjectMocks
    private JobService jobService;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private JobRepository jobRepository;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private MailSenderService mailSenderService;

    @Test
    void testCancelJobRequest_WithValidRequest_ShouldCancelJob() throws Exception {
        // Given
        CustomerEntity customer = new CustomerEntity(
                "customerId",
                "Jane",
                "Doe",
                CustomerType.PRIVATE,
                "Jane Street 1",
                12345,
                "Jane City",
                "076-250 90 80",
                "jane.doe@janecity.com",
                "password",
                null);

        JobEntity job = new JobEntity(
                "jobId",
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 5),
                customer,
                null,
                JobType.BASIC_CLEANING,
                "I want you to clean my car aswell",
                JobStatus.OPEN,
                null);

        CancelJobRequest cancelJobRequest = new CancelJobRequest(customer.getId(), job.getId());
        //When
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(jobRepository.findById(anyString())).thenReturn(Optional.of(job));
        boolean result = jobService.cancelJobRequest(cancelJobRequest);
        // Then
        assertTrue(result);
        verify(jobRepository, times(1)).deleteById(anyString());
    }

    @Test
    void testCancelJobRequest_WithInvalidCustomer_ShouldThrowNotFoundException() {
        // Given
        CancelJobRequest cancelJobRequest = new CancelJobRequest("invalidCustomerId", "jobId");
        //When
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());
        // Then
        assertThrows(NotFoundException.class, () -> {
            jobService.cancelJobRequest(cancelJobRequest);
        });
        verify(jobRepository, never()).deleteById(anyString());
    }

    @Test
    void testCancelJobRequest_WithInvalidJobId_ShouldThrowNotFoundException() {
        // Given
        CancelJobRequest cancelJobRequest = new CancelJobRequest("customerId", "invalidJobId");
        // When
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(new CustomerEntity()));
        when(jobRepository.findById(anyString())).thenReturn(Optional.empty());
        // Then
        assertThrows(NotFoundException.class, () -> {
            jobService.cancelJobRequest(cancelJobRequest);
        });
        verify(jobRepository, never()).deleteById(anyString());
    }

    @Test
    void testCancelJobRequest_WithUnauthorizedCustomer_ShouldThrowUnauthorizedCallException() {
        // Given
        CancelJobRequest cancelJobRequest = new CancelJobRequest("customerId", "jobId");
        CustomerEntity customer = new CustomerEntity();
        JobEntity job = new JobEntity();
        job.setCustomer(new CustomerEntity());
        // When
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(jobRepository.findById(anyString())).thenReturn(Optional.of(job));
        // Then
        assertThrows(UnauthorizedCallException.class, () -> {
            jobService.cancelJobRequest(cancelJobRequest);
        });
        verify(jobRepository, never()).deleteById(anyString());
    }

    @Test
    void testCancelJobRequest_WithCompletedJob_ShouldThrowUnauthorizedCallException() {
        // Given
        CancelJobRequest cancelJobRequest = new CancelJobRequest("customerId", "jobId");
        CustomerEntity customer = new CustomerEntity();
        JobEntity job = new JobEntity();
        job.setCustomer(customer);
        job.setStatus(JobStatus.CLOSED);
        // When
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(jobRepository.findById(anyString())).thenReturn(Optional.of(job));
        // Then
        assertThrows(UnauthorizedCallException.class, () -> {
            jobService.cancelJobRequest(cancelJobRequest);
        });
        verify(jobRepository, never()).deleteById(anyString());
    }

    @Test
    void sendEmailConfirmationCanceledJob_EmailSentSuccessfully() {

        JobEntity requestedCancel = new JobEntity(
                null,
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 5),
                new CustomerEntity(
                        null,
                        "Jane",
                        "Doe",
                        CustomerType.PRIVATE,
                        "Jane Street 1",
                        12345,
                        "Jane City",
                        "076-250 90 80",
                        "jane.doe@janecity.com",
                        "password",
                        null
                ),
                List.of(),
                JobType.BASIC_CLEANING,
                "I want you to clean my car aswell",
                JobStatus.OPEN,
                null
        );

        mailSenderService.sendEmailConfirmationCanceledJob(requestedCancel);

        verify(mailSenderService, times(1)).sendEmailConfirmationBookedJob(requestedCancel);
    }
}
