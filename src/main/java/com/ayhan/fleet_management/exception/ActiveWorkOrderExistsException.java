package com.ayhan.fleet_management.exception;

public class ActiveWorkOrderExistsException extends RuntimeException {

    public ActiveWorkOrderExistsException(String message) {
        super(message);
    }
}
