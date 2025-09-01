package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.Domain.OrderItem;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.DTO.*;
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

    @Autowired
    private DTOMapper dtoMapper;

    // Basic CRUD operations - now returning DTOs with optimized queries
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllWithOrderItems();
        return dtoMapper.toOrderDTOList(orders);
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(id);
        return order.map(dtoMapper::toOrderDTO);
    }

    public OrderDTO saveOrder(OrderDTO orderDTO) {
        Order order = dtoMapper.toOrderEntity(orderDTO);
        Order savedOrder = orderRepository.save(order);
        return dtoMapper.toOrderDTO(savedOrder);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // Order search and filtering - now using optimized queries where appropriate
    public Optional<OrderDTO> findByOrderNumber(String orderNumber) {
        Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
        // If order exists, fetch it with order items for complete DTO mapping
        if (order.isPresent()) {
            Optional<Order> orderWithItems = orderRepository.findByIdWithOrderItems(order.get().getId());
            return orderWithItems.map(dtoMapper::toOrderDTO);
        }
        return Optional.empty();
    }

    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        // For multiple orders, use the optimized query if we need order items
        // Otherwise, use the simple query to avoid unnecessary joins
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCustomerOrdersByStatus(Long customerId, String status) {
        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return dtoMapper.toOrderDTOList(orders);
    }

    // Order item management - optimized to reduce queries
    public List<OrderItemDTO> getOrderItems(Long orderId) {
        // Use the optimized query to get order with items in one query
        Optional<Order> order = orderRepository.findByIdWithOrderItems(orderId);
        if (order.isPresent()) {
            return dtoMapper.toOrderItemDTOList(order.get().getOrderItems());
        }
        return List.of();
    }

    public List<OrderItemDTO> findOrderItemsByProductName(String productName) {
        List<OrderItem> orderItems = orderItemRepository.findByProductName(productName);
        return dtoMapper.toOrderItemDTOList(orderItems);
    }

    // Order creation and processing - now using optimized queries
    @Transactional
    public OrderDTO createOrder(Long customerId, List<OrderItemDTO> orderItemDTOs) {
        // Use optimized customer query if available
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new RuntimeException("Customer not found with id: " + customerId);
        }

        Customer customer = customerOpt.get();

        // Create new order
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus("PENDING");
        order.setCustomer(customer);

        // Calculate total amount and validate stock
        double totalAmount = 0.0;
        List<OrderItem> orderItems = orderItemDTOs.stream()
                .map(dto -> {
                    OrderItem item = dtoMapper.toOrderItemEntity(dto);
                    item.setOrder(order);
                    return item;
                })
                .toList();

        for (OrderItem item : orderItems) {
            // Validate product availability
            if (!productService.isProductAvailable(getProductIdFromName(item.getProductName()), item.getQuantity())) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
            }

            totalAmount += item.getPrice() * item.getQuantity();
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

        // Return the order with all items loaded
        Optional<Order> orderWithItems = orderRepository.findByIdWithOrderItems(savedOrder.getId());
        return orderWithItems.map(dtoMapper::toOrderDTO)
                .orElse(dtoMapper.toOrderDTO(savedOrder));
    }

    // Order status management - now using optimized queries
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            Order savedOrder = orderRepository.save(order);
            return dtoMapper.toOrderDTO(savedOrder);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    public OrderDTO processOrder(Long orderId) {
        return updateOrderStatus(orderId, "PROCESSING");
    }

    public OrderDTO shipOrder(Long orderId) {
        return updateOrderStatus(orderId, "SHIPPED");
    }

    public OrderDTO deliverOrder(Long orderId) {
        return updateOrderStatus(orderId, "DELIVERED");
    }

    public OrderDTO cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
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
                Order savedOrder = orderRepository.save(order);
                return dtoMapper.toOrderDTO(savedOrder);
            } else {
                throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
            }
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    // Order item operations - using optimized queries
    public OrderItemDTO addOrderItem(Long orderId, OrderItemDTO orderItemDTO) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only add items to pending orders
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("Cannot add items to order with status: " + order.getStatus());
            }

            OrderItem orderItem = dtoMapper.toOrderItemEntity(orderItemDTO);
            orderItem.setOrder(order);
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Update order total
            order.setTotalAmount(order.getTotalAmount() + (orderItem.getPrice() * orderItem.getQuantity()));
            orderRepository.save(order);

            return dtoMapper.toOrderItemDTO(savedItem);
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    // Order calculations - using optimized queries
    public Double calculateOrderTotal(Long orderId) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(orderId);
        if (order.isPresent()) {
            return order.get().getOrderItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
        return 0.0;
    }

    public Integer getOrderItemCount(Long orderId) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(orderId);
        return order.map(o -> o.getOrderItems().size()).orElse(0);
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

    // Order validation - using optimized queries
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

    // Update order - using optimized queries
    public OrderDTO updateOrder(Long id, OrderDTO updatedOrderDTO) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only update if order is still pending
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException("Cannot update order with status: " + order.getStatus());
            }

            order.setStatus(updatedOrderDTO.getStatus());
            if (updatedOrderDTO.getTotalAmount() != null) {
                order.setTotalAmount(updatedOrderDTO.getTotalAmount());
            }
            Order savedOrder = orderRepository.save(order);
            return dtoMapper.toOrderDTO(savedOrder);
        }
        throw new RuntimeException("Order not found with id: " + id);
    }

    // Add new methods to leverage specific repository queries
    public List<OrderDTO> getCompletedOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findCompletedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getPendingOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findPendingOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCancelledOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findCancelledOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getShippedOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findShippedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getDeliveredOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findDeliveredOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getReturnedOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findReturnedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getRefundedOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findRefundedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getOrdersAboveAmount(Double amount) {
        List<Order> orders = orderRepository.findByTotalAmountGreaterThan(amount);
        return dtoMapper.toOrderDTOList(orders);
    }
}
