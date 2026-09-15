package com.mesofi.mythclothapi.error.exceptions;

import java.io.Serial;

import org.springframework.http.HttpStatus;

import com.mesofi.mythclothapi.integration.ServiceName;

import lombok.Getter;

@Getter
public class IntegrationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -526790961162718694L;

    private final ServiceName serviceName;
    private final HttpStatus status;
    private final String message;

    public IntegrationException(ServiceName serviceName, int code, String message) {
        this(serviceName, HttpStatus.valueOf(code), message);
    }

    public IntegrationException(ServiceName serviceName, HttpStatus status, String message) {
        super(message);
        this.serviceName = serviceName;
        this.status = status;
        this.message = message;
    }
}
