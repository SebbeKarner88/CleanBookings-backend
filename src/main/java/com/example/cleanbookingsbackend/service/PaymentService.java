package com.example.cleanbookingsbackend.service;

import com.example.cleanbookingsbackend.model.JobEntity;
import com.example.cleanbookingsbackend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void createInvoiceOnJob(JobEntity job) {


    }

}
