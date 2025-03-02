package com.spribe.bookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UnitAlreadyBookedException extends RuntimeException {
    public UnitAlreadyBookedException(String message) {
        super(message);
    }
}
