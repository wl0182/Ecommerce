package com.wassimlagnaoui.Ecommerce.Domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    private String id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
