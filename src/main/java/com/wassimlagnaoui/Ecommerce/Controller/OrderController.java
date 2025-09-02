package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Order Management", description = "APIs for managing orders and order items in the ecommerce system")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Get all orders", description = "Retrieve a list of all orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        Optional<OrderDTO> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new order", description = "Place a new order in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data")
    })
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO order) {
        try {
            OrderDTO savedOrder = orderService.saveOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update an existing order", description = "Modify the details of an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderDTO order) {
        try {
            OrderDTO updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Delete an order", description = "Remove an order from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get order by order number", description = "Retrieve a specific order using its order number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/order-number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Optional<OrderDTO> order = orderService.findByOrderNumber(orderNumber);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get orders by customer", description = "Retrieve all orders placed by a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found"),
            @ApiResponse(responseCode = "404", description = "No orders found for this customer")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByCustomer(@PathVariable Long customerId) {
        List<OrderDTO> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found"),
            @ApiResponse(responseCode = "404", description = "No orders found with this status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDTO>> getOrdersByStatus(@PathVariable String status) {
        List<OrderDTO> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get customer orders by status", description = "Retrieve all orders of a customer with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found"),
            @ApiResponse(responseCode = "404", description = "No orders found for this customer with the given status")
    })
    @GetMapping("/customer/{customerId}/status/{status}")
    public ResponseEntity<List<OrderDTO>> getCustomerOrdersByStatus(
            @PathVariable Long customerId,
            @PathVariable String status) {
        List<OrderDTO> orders = orderService.getCustomerOrdersByStatus(customerId, status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get order items", description = "Retrieve all items of a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order items found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemDTO>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItemDTO> orderItems = orderService.getOrderItems(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @Operation(summary = "Find order items by product name", description = "Retrieve all order items containing a specific product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order items found"),
            @ApiResponse(responseCode = "404", description = "No order items found for this product")
    })
    @GetMapping("/items/product/{productName}")
    public ResponseEntity<List<OrderItemDTO>> findOrderItemsByProductName(@PathVariable String productName) {
        List<OrderItemDTO> orderItems = orderService.findOrderItemsByProductName(productName);
        return ResponseEntity.ok(orderItems);
    }

    @Operation(summary = "Add an order item", description = "Include a new item to an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order item data"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderItemDTO> addOrderItem(@PathVariable Long orderId, @RequestBody OrderItemDTO orderItem) {
        try {
            OrderItemDTO savedItem = orderService.addOrderItem(orderId, orderItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Create a new order with items", description = "Place a new order along with its items in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order or item data")
    })
    @PostMapping("/create")
    public ResponseEntity<OrderDTO> createOrder(@RequestParam Long customerId, @RequestBody List<OrderItemDTO> orderItems) {
        try {
            OrderDTO createdOrder = orderService.createOrder(customerId, orderItems);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update order status", description = "Change the status of an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Process an order", description = "Mark an order as processed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/process")
    public ResponseEntity<OrderDTO> processOrder(@PathVariable Long orderId) {
        try {
            OrderDTO processedOrder = orderService.processOrder(orderId);
            return ResponseEntity.ok(processedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Ship an order", description = "Mark an order as shipped")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order shipped"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<OrderDTO> shipOrder(@PathVariable Long orderId) {
        try {
            OrderDTO shippedOrder = orderService.shipOrder(orderId);
            return ResponseEntity.ok(shippedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Deliver an order", description = "Mark an order as delivered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order delivered"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<OrderDTO> deliverOrder(@PathVariable Long orderId) {
        try {
            OrderDTO deliveredOrder = orderService.deliverOrder(orderId);
            return ResponseEntity.ok(deliveredOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Cancel an order", description = "Mark an order as cancelled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId) {
        try {
            OrderDTO cancelledOrder = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(cancelledOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Calculate order total", description = "Compute the total amount for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total amount calculated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/total")
    public ResponseEntity<Double> calculateOrderTotal(@PathVariable Long orderId) {
        Double total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }

    @Operation(summary = "Get order item count", description = "Retrieve the number of items in an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item count retrieved"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/item-count")
    public ResponseEntity<Integer> getOrderItemCount(@PathVariable Long orderId) {
        Integer itemCount = orderService.getOrderItemCount(orderId);
        return ResponseEntity.ok(itemCount);
    }

    @Operation(summary = "Check if order can be processed", description = "Determine if an order is eligible for processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order can be processed"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/can-process")
    public ResponseEntity<Boolean> canProcessOrder(@PathVariable Long orderId) {
        boolean canProcess = orderService.canProcessOrder(orderId);
        return ResponseEntity.ok(canProcess);
    }

    @Operation(summary = "Check if order can be cancelled", description = "Determine if an order is eligible for cancellation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order can be cancelled"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/can-cancel")
    public ResponseEntity<Boolean> canCancelOrder(@PathVariable Long orderId) {
        boolean canCancel = orderService.canCancelOrder(orderId);
        return ResponseEntity.ok(canCancel);
    }
}
