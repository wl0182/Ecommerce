package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private String id;
    private String productName;
    private Integer quantity;
    private Double price;
    private String orderId;
}
