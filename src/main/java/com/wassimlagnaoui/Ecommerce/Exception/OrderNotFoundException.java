package com.wassimlagnaoui.Ecommerce.Exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }

    public OrderNotFoundException(String orderNumber, boolean isOrderNumber) {
        super("Order not found with order number: " + orderNumber);
    }
}
