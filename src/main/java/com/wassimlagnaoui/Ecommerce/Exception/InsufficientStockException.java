package com.wassimlagnaoui.Ecommerce.Exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableStock) {
        super("Insufficient stock for product: " + productName +
              ". Requested: " + requestedQuantity + ", Available: " + availableStock);
    }
}
