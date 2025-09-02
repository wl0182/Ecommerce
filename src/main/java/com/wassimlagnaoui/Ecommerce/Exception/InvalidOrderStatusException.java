package com.wassimlagnaoui.Ecommerce.Exception;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String currentStatus, String attemptedAction) {
        super("Cannot " + attemptedAction + " order with status: " + currentStatus);
    }
}
