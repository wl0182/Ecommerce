package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String id;
    private Integer rating;
    private String comment;
    private String productId;
    private String productName;
    private String customerId;
    private String customerName;
}
