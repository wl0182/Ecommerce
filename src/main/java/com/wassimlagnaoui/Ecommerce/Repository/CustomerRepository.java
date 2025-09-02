package com.wassimlagnaoui.Ecommerce.Repository;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Customer> findByName(String name);

    // Find customers who spent more than a certain amount
    @Query("SELECT c FROM Customer c join FETCH c.orders WHERE c.totalSpent > ?1")
    List<Customer> findByTotalSpentGreaterThan(Double amount);

    // Account for lazy loading of orders
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders WHERE c.id = ?1")
    Optional<Customer> findByIdWithOrders(String id);



    @Query ("SELECT c FROM Customer c LEFT JOIN FETCH c.reviews")
    List<Customer> findAllWithReviews();

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.reviews WHERE c.id = ?1")
    Optional<Customer> findByIdWithReviews(String id);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders LEFT JOIN FETCH c.addresses LEFT JOIN FETCH c.reviews WHERE c.id = ?1")
    Optional<Customer> findByIdWithAllDetails(String id);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.orders LEFT JOIN FETCH c.addresses LEFT JOIN FETCH c.reviews")
    List<Customer> findAllWithAllDetails();


    // Add Specific queries
    @Query("SELECT c FROM Customer c WHERE SIZE(c.orders) > ?1" )
    List<Customer> findCustomersWithMoreThanNOrders(int n);


}
