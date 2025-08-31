package com.wassimlagnaoui.Ecommerce.Domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table( name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private Long id;
    private String productName;
    private Integer quantity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
