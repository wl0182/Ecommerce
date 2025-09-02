package com.wassimlagnaoui.Ecommerce.Exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }



    public CategoryNotFoundException(Long categoryId) {
        super("Category not found with id: " + categoryId);
    }
}
