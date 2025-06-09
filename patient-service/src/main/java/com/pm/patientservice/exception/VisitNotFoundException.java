package com.pm.patientservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VisitNotFoundException extends RuntimeException {
    public VisitNotFoundException(String message) {
        super(message);
    }
} 