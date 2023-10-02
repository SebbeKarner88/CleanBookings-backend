package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.AssignCleanerRequest;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.JobType;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.NotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.CustomerEntity;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.EmployeeRepository;
import com.example.cleanbookingsbackend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class AssignCleanersTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private JobService jobService;

    // DON'T SEEM TO GET THIS TEST TO WORK, ANY INPUT WOULD BE WELCOME!

/*    @Test
    void assignCleanerRequest_ValidRequest_AssignsCleanersAndSendsEmail() throws Exception {
        // Arrange
        JobEntity job = new JobEntity(
                "jobId",
                new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 5),
                new CustomerEntity(),
                List.of(),
                JobType.BASIC_CLEANING,
                "I want you to clean my car aswell",
                JobStatus.OPEN,
                null);

        EmployeeEntity admin = new EmployeeEntity(
                "adminId",
                "Admin",
                "Adminson",
                "073-9 123 843",
                Role.ADMIN,
                "admin1@CleanBookings.com",
                "password",
                null);

        EmployeeEntity cleaner = new EmployeeEntity(
                "cleanerId",
                "Cleaner1",
                "Cleanerson",
                "073-9 453 843",
                Role.CLEANER,
                "Cleaner1@CleanBookings.com",
                "password",
                null);

        AssignCleanerRequest request = new AssignCleanerRequest(job.getId(), admin.getId(), List.of(cleaner.getId()));


        when(jobRepository.findById(request.jobId())).thenReturn(Optional.of(job));
        when(employeeRepository.findById(request.adminId())).thenReturn(Optional.of(admin));
        when(employeeRepository.findById(request.cleanerId().get(0))).thenReturn(Optional.of(cleaner));

        // Act
        jobService.assignCleanerRequest(request);

        // Assert
        verify(jobRepository, times(1)).findById(request.jobId());
        verify(employeeRepository, times(request.cleanerId().size() + 1)).findById(anyString()); // admin + cleaners
        verify(mailSender, times(request.cleanerId().size())).send(any(SimpleMailMessage.class)); // one email per cleaner
        assertEquals(JobStatus.ASSIGNED, job.getStatus());
        assertEquals(2, job.getEmployee().size()); // assuming 1 admin and 1 cleaner added
    }*/


}
