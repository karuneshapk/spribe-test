package com.spribe.bookingsystem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UnitNotFoundException extends RuntimeException {
    public UnitNotFoundException(Integer unitId) {
        super("Unit not found with ID: " + unitId);
    }
}
