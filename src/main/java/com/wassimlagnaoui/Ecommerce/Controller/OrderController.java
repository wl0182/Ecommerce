package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Basic CRUD operations - now using DTOs
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Optional<OrderDTO> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO order) {
        try {
            OrderDTO savedOrder = orderService.saveOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO order) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Order search and filtering - now using DTOs
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Optional<OrderDTO> order = orderService.findByOrderNumber(orderNumber);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<OrderDTO>> getCustomerOrdersByStatus(
            @PathVariable Long customerId,
            @PathVariable String status) {
        List<OrderDTO> orders = orderService.getCustomerOrdersByStatus(customerId, status);
        return ResponseEntity.ok(orders);
    }

    // Order item management - now using DTOs
    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItemDTO> orderItems = orderService.getOrderItems(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/items/product/{productName}")
    public ResponseEntity<List<OrderItemDTO>> findOrderItemsByProductName(@PathVariable String productName) {
        List<OrderItemDTO> orderItems = orderService.findOrderItemsByProductName(productName);
        return ResponseEntity.ok(orderItems);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemDTO> addOrderItem(@PathVariable Long orderId, @RequestBody OrderItemDTO orderItem) {
        try {
            OrderItemDTO savedItem = orderService.addOrderItem(orderId, orderItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Order creation and processing - now using DTOs
    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestParam Long customerId, @RequestBody List<OrderItemDTO> orderItems) {
        try {
            OrderDTO createdOrder = orderService.createOrder(customerId, orderItems);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Order status management - now using DTOs
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{orderId}/process")
    public ResponseEntity<OrderDTO> processOrder(@PathVariable Long orderId) {
        try {
            OrderDTO processedOrder = orderService.processOrder(orderId);
            return ResponseEntity.ok(processedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/ship")
    public ResponseEntity<OrderDTO> shipOrder(@PathVariable Long orderId) {
        try {
            OrderDTO shippedOrder = orderService.shipOrder(orderId);
            return ResponseEntity.ok(shippedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDTO> deliverOrder(@PathVariable Long orderId) {
        try {
            OrderDTO deliveredOrder = orderService.deliverOrder(orderId);
            return ResponseEntity.ok(deliveredOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        try {
            OrderDTO cancelledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Order calculations and metrics
    @GetMapping("/{orderId}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Long orderId) {
        Double total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{orderId}/item-count")
    public ResponseEntity<Integer> getOrderItemCount(@PathVariable Long orderId) {
        Integer itemCount = orderService.getOrderItemCount(orderId);
        return ResponseEntity.ok(itemCount);
    }

    // Order validation
    @GetMapping("/{orderId}/can-process")
    public ResponseEntity<Boolean> canProcessOrder(@PathVariable Long orderId) {
        boolean canProcess = orderService.canProcessOrder(orderId);
        return ResponseEntity.ok(canProcess);
    }

    @GetMapping("/{orderId}/can-cancel")
    public ResponseEntity<Boolean> canCancelOrder(@PathVariable Long orderId) {
        boolean canCancel = orderService.canCancelOrder(orderId);
        return ResponseEntity.ok(canCancel);
    }
}
