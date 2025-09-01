package com.wassimlagnaoui.Ecommerce.Repository;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Customer> findByName(String name);

    // Find customers who spent more than a certain amount
    @Query("SELECT c FROM Customer c WHERE c.totalSpent > ?1")
    List<Customer> findByTotalSpentGreaterThan(Double amount);
}
