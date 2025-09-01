package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stock;
    private Double averageRating;
    private Long reviewCount;
}
