package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.Domain.OrderItem;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.Repository.CustomerRepository;
import com.wassimlagnaoui.Ecommerce.Repository.OrderItemRepository;
import com.wassimlagnaoui.Ecommerce.Repository.OrderRepository;
import com.wassimlagnaoui.Ecommerce.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerService customerService;

    // Basic CRUD operations
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // Order search and filtering
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getCustomerOrdersByStatus(Long customerId, String status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }

    // Order item management
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public List<OrderItem> findOrderItemsByProductName(String productName) {
        return orderItemRepository.findByProductName(productName);
    }

    // Order creation and processing
    @Transactional
    public Order createOrder(Long customerId, List<OrderItem> orderItems) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (!customerOpt.isPresent()) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }

        Customer customer = customerOpt.get();

        // Create new order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus("PENDING");
        order.setCustomer(customer);

        // Calculate total amount and validate stock
        Double totalAmount = 0.0;
        for (OrderItem item : orderItems) {
            // Validate product availability
            if (!productService.isProductAvailable(getProductIdFromName(item.getProductName()), item.getQuantity())) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
            }

            totalAmount += item.getPrice() * item.getQuantity();
            item.setOrder(order);
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Update product stock and sales count
        for (OrderItem item : orderItems) {
            Long productId = getProductIdFromName(item.getProductName());
            productService.reduceStock(productId, item.getQuantity());
            productService.increaseSalesCount(productId, item.getQuantity());
        }

        // Update customer total spent
        customerService.updateTotalSpent(customerId, totalAmount);

        return savedOrder;
    }

    // Order status management
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public Order processOrder(Long orderId) {
        return updateOrderStatus(orderId, "PROCESSING");
    }

    public Order shipOrder(Long orderId) {
        return updateOrderStatus(orderId, "SHIPPED");
    }

    public Order deliverOrder(Long orderId) {
        return updateOrderStatus(orderId, "DELIVERED");
    }

    public Order cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only cancel if order is still pending or processing
            if ("PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus())) {
                // Restore product stock
                for (OrderItem item : order.getOrderItems()) {
                    Long productId = getProductIdFromName(item.getProductName());
                    Optional<Product> productOpt = productRepository.findById(productId);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        product.setStock(product.getStock() + item.getQuantity());
                        productRepository.save(product);
                    }
                }

                // Revert customer total spent
                Customer customer = order.getCustomer();
                customer.setTotalSpent(customer.getTotalSpent() - order.getTotalAmount());
                customerRepository.save(customer);

                order.setStatus("CANCELLED");
                return orderRepository.save(order);
            } else {
                throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
            }
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    // Order item operations
    public OrderItem addOrderItem(Long orderId, OrderItem orderItem) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only add items to pending orders
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("Cannot add items to order with status: " + order.getStatus());
            }

            orderItem.setOrder(order);
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Update order total
            order.setTotalAmount(order.getTotalAmount() + (orderItem.getPrice() * orderItem.getQuantity()));
            orderRepository.save(order);

            return savedItem;
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    // Order calculations
    public Double calculateOrderTotal(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public Integer getOrderItemCount(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).size();
    }

    // Utility methods
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long getProductIdFromName(String productName) {
        Optional<Product> product = productRepository.findByName(productName);
        if (product.isPresent()) {
            return product.get().getId();
        }
        throw new RuntimeException("Product not found: " + productName);
    }

    // Order validation
    public boolean canProcessOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isPresent() && "PENDING".equals(order.get().getStatus());
    }

    public boolean canCancelOrder(Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            String status = order.get().getStatus();
            return "PENDING".equals(status) || "PROCESSING".equals(status);
        }
        return false;
    }

    // Update order
    public Order updateOrder(Long id, Order updatedOrder) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only update if order is still pending
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("Cannot update order with status: " + order.getStatus());
            }

            order.setStatus(updatedOrder.getStatus());
            if (updatedOrder.getTotalAmount() != null) {
                order.setTotalAmount(updatedOrder.getTotalAmount());
            }
            return orderRepository.save(order);
        }
        throw new RuntimeException("Order not found with id: " + id);
    }
}
