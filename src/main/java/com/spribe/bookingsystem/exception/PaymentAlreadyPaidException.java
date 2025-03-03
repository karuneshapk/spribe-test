package com.spribe.bookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentAlreadyPaidException extends RuntimeException {
    public PaymentAlreadyPaidException(Integer paymentId) {
        super("Payment is already paid with ID: " + paymentId);
    }
}
