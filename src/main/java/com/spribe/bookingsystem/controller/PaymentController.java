package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PutMapping("/{paymentId}/confirm")
    public ResponseEntity<String> confirmPayment(@PathVariable int paymentId) {
        paymentService.processPayment(paymentId, true);
        return ResponseEntity.ok("Payment confirmed");
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<String> cancelPayment(@PathVariable int paymentId) {
        paymentService.processPayment(paymentId, false);
        return ResponseEntity.ok("Payment canceled");
    }
}
