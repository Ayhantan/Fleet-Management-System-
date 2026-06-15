package com.ayhan.fleet_management.exception;

public class InvalidWorkOrderStateException extends RuntimeException {

    public InvalidWorkOrderStateException(String message) {
        super(message);
    }
}
