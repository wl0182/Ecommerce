package com.wassimlagnaoui.Ecommerce.Exception;

public class DuplicateProductNameException extends RuntimeException {
    public DuplicateProductNameException(String productName) {
        super("Product name already exists: " + productName);
    }
}
