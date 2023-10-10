package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.enums.PaymentStatus;
import com.example.cleanbookingsbackend.exception.JobNotFoundException;
import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.model.PaymentEntity;
import com.example.cleanbookingsbackend.repository.JobRepository;
import com.example.cleanbookingsbackend.repository.PaymentRepository;
import com.example.cleanbookingsbackend.service.utils.InputValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final JobRepository jobRepository;
    private final InputValidation input;

    public void createInvoiceOnJob(JobEntity job) throws JobNotFoundException {
        PaymentEntity invoice = new PaymentEntity(
                null,
                new Date(System.currentTimeMillis()),
                null,
                job,
                PaymentStatus.INVOICED,
                solvePrice(job)
        );
        paymentRepository.save(invoice);

        JobEntity updatedJob = input.validateJobId(job.getId());
        updatedJob.setPayment(invoice);
        jobRepository.save((updatedJob));
    }

    private static double solvePrice(JobEntity job) {
        return switch (job.getType()) {
            case BASIC_CLEANING -> 795d;
            case TOPP_CLEANING -> 1495d;
            case DIAMOND_CLEANING -> 2495d;
            case WINDOW_CLEANING -> 495d;
        };
    }
}

