package com.claropr.exceptions;

import lombok.Getter;

@Getter
public class BusinessLogicException extends RuntimeException {
    private final int statusCode;
    private final String message;

    public BusinessLogicException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }
}
