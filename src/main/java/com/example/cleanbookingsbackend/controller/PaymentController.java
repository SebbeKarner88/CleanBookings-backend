package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @PutMapping("/markAsPaid")
    public ResponseEntity<?> markAsPaid(@RequestParam String adminId,
                                        @RequestParam String invoiceId) {
        try {
            paymentService.markInvoiceAsPaid(adminId, invoiceId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (EmployeeNotFoundException | PaymentNotFoundException | JobNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }
}
