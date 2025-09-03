package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    @PositiveOrZero(message = "Total amount must be zero or positive")
    private Double totalAmount;
}
