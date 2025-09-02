package com.wassimlagnaoui.Ecommerce.Exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String productName, boolean isName) {
        super("Product not found with name: " + productName);
    }
}
