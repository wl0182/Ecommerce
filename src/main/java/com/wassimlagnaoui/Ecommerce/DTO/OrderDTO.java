package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String orderNumber;
    private String status;
    private Double totalAmount;
    private String customerId;
    private String customerName;
    private List<OrderItemDTO> orderItems;
}
