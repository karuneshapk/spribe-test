package com.spribe.bookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentAlreadyFailedException extends RuntimeException {
    public PaymentAlreadyFailedException(Integer paymentId) {
        super("Payment is already failed with ID: " + paymentId);
    }
}
