package com.wassimlagnaoui.Ecommerce.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDTO {
    private Long id;
    private String name;
    private String email;
    private Double totalSpent;
    private Integer orderCount;
}
