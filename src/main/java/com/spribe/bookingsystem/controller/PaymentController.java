package com.spribe.bookingsystem.controller;

import com.spribe.bookingsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestParam int paymentId, @RequestParam boolean success) {
        paymentService.processPayment(paymentId, success);
        return ResponseEntity.ok("Payment processed");
    }
}
