package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.enums.PaymentStatus;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
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

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final JobRepository jobRepository;
    private final InputValidation input;
    private final MailSenderService mailSender;

    public void createInvoiceOnJob(JobEntity job) throws JobNotFoundException {
        PaymentEntity invoice = createInvoice(job);
        JobEntity updatedJob = input.validateJobId(job.getId());
        updatedJob.setPayment(invoice);
        jobRepository.save((updatedJob));
        paymentRepository.save(invoice);
        mailSender.sendInvoice(job);
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
}

