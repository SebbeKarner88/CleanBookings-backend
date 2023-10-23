package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.dto.JobResponseDTO;
import com.example.cleanbookingsbackend.dto.PaymentDTO;
import com.example.cleanbookingsbackend.enums.JobStatus;
import com.example.cleanbookingsbackend.enums.PaymentStatus;
import com.example.cleanbookingsbackend.enums.Role;
import com.example.cleanbookingsbackend.exception.EmployeeNotFoundException;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.exception.PaymentNotFoundException;
import com.example.cleanbookingsbackend.exception.UnauthorizedCallException;
import com.example.cleanbookingsbackend.model.EmployeeEntity;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.repository.PaymentRepository;
import com.example.cleanbookingsbackend.service.utils.InputValidation;
import com.example.cleanbookingsbackend.service.utils.MailSenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final JobRepository jobRepository;
    private final EmployeeService employeeService;
    private final InputValidation input;
    private final MailSenderService mailSender;

    public void createInvoiceOnJob(JobEntity job) throws JobNotFoundException {
        PaymentEntity invoice = createInvoice(job);
        JobEntity updatedJob = input.validateJobId(job.getId());
        updatedJob.setPayment(invoice);
        paymentRepository.save(invoice);
        jobRepository.save(updatedJob);
        mailSender.sendInvoice(job);
    }

    public List<PaymentDTO> getAllInvoices(String adminId)
            throws UnauthorizedCallException, EmployeeNotFoundException {
        if (!employeeService.isAdmin(adminId))
            throw new UnauthorizedCallException("Only a admin is authorized to make this call.");
        return paymentRepository
                .findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public void markInvoiceAsPaid(String adminId, String invoiceId)
            throws UnauthorizedCallException, JobNotFoundException, EmployeeNotFoundException, PaymentNotFoundException {
        EmployeeEntity admin = input.validateEmployeeId(adminId);
        PaymentEntity invoice = input.validatePaymentId(invoiceId);
        JobEntity job = input.validateJobId(invoice.getJob().getId());

        if (!admin.getRole().equals(Role.ADMIN))
            throw new UnauthorizedCallException("Only a Admin can mark invoices as paid");

        invoice.setStatus(PaymentStatus.PAID);
        paymentRepository.save(invoice);
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
        mailSender.sendEmailConfirmationOnPaidInvoice(job);
    }

    private static PaymentEntity createInvoice(JobEntity job) {
        return new PaymentEntity(
                null,
                new Date(System.currentTimeMillis()),
                null,
                job,
                PaymentStatus.INVOICED,
                solvePrice(job)
        );
    }

    private static double solvePrice(JobEntity job) {
        return switch (job.getType()) {
            case BASIC_CLEANING -> 795d;
            case TOPP_CLEANING -> 1495d;
            case DIAMOND_CLEANING -> 2495d;
            case WINDOW_CLEANING -> 495d;
        };
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void updatePaymentStatusForDuePayments() {
        List<PaymentEntity> payments = paymentRepository.findByDueDateBeforeAndStatus(new Date(), PaymentStatus.INVOICED);

        for (PaymentEntity payment : payments) {
            payment.setStatus(PaymentStatus.OVERDUE);
            paymentRepository.save(payment);
        }
    }

    private PaymentDTO toDTO(PaymentEntity payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getIssueDate(),
                payment.getDueDate(),
                payment.getJob().getId(),
                payment.getStatus(),
                payment.getPrice()
        );
    }

}

