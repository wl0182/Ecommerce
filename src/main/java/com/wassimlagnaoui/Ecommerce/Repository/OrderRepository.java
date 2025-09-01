package com.wassimlagnaoui.Ecommerce.Repository;

import com.wassimlagnaoui.Ecommerce.Domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(String status);
    List<Order> findByCustomerIdAndStatus(Long customerId, String status);

    // Account for lazy loading of order items and avoid N+1 problem

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = ?1")
    Optional<Order> findByIdWithOrderItems(Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems")
    List<Order> findAllWithOrderItems();

    // adding  other important queries

    @Query("SELECT o FROM Order o WHERE o.totalAmount > ?1")
    List<Order> findByTotalAmountGreaterThan(Double amount);

    @Query("SELECT o FROM Order o WHERE o.status = 'COMPLETED' AND o.customer.id = ?1")
    List<Order> findCompletedOrdersByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.customer.id = ?1")
    List<Order> findPendingOrdersByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = 'CANCELLED' AND o.customer.id = ?1")
    List<Order> findCancelledOrdersByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND o.customer.id = ?1")
    List<Order> findShippedOrdersByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = 'DELIVERED' AND o.customer.id = ?1")
    List<Order> findDeliveredOrdersByCustomerId(Long customerId);


    @Query("SELECT o FROM Order o WHERE o.status = 'RETURNED' AND o.customer.id = ?1")
    List<Order> findReturnedOrdersByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE o.status = 'REFUNDED' AND o.customer.id = ?1")
    List<Order> findRefundedOrdersByCustomerId(Long customerId);








}
