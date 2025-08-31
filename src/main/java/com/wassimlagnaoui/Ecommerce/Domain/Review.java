package com.wassimlagnaoui.Ecommerce.Domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private Long id;
    private Integer rating;
    private String comment;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;




}
