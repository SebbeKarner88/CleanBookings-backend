package com.example.cleanbookingsbackend.controller;

import com.example.cleanbookingsbackend.dto.PaymentDTO;
import com.example.cleanbookingsbackend.exception.*;
import com.example.cleanbookingsbackend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/payment")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("getAllInvoices")
    public ResponseEntity<?> getAllInvoices(@RequestParam String adminId) {
        try {
            List<PaymentDTO> invoices = paymentService.getAllInvoices(adminId);
            return ResponseEntity.ok().body(invoices);
        } catch (EmployeeNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(
            @PathVariable("id") String invoiceId,
            @RequestParam String adminId
    ) {
        try {
            paymentService.deleteInvoice(adminId, invoiceId);
            return ResponseEntity.ok().build();
        } catch (EmployeeNotFoundException | PaymentNotFoundException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
        } catch (UnauthorizedCallException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

}
