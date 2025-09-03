package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.Domain.OrderItem;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Exception.CustomerNotFoundException;
import com.wassimlagnaoui.Ecommerce.Exception.InsufficientStockException;
import com.wassimlagnaoui.Ecommerce.Exception.InvalidOrderStatusException;
import com.wassimlagnaoui.Ecommerce.Exception.OrderNotFoundException;
import com.wassimlagnaoui.Ecommerce.Exception.ProductNotFoundException;
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

    public Optional<OrderDTO> getOrderById(String id) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(id);
        return order.map(dtoMapper::toOrderDTO);
    }

    public OrderDTO saveOrder(OrderDTO orderDTO) {
        Order order = dtoMapper.toOrderEntity(orderDTO);
        Order savedOrder = orderRepository.save(order);
        return dtoMapper.toOrderDTO(savedOrder);
    }

    public void deleteOrder(String id) {
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

    public List<OrderDTO> getOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        // For multiple orders, use the optimized query if we need order items
        // Otherwise, use the simple query to avoid unnecessary joins
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCustomerOrdersByStatus(String customerId, String status) {
        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return dtoMapper.toOrderDTOList(orders);
    }

    // Order item management - optimized to reduce queries
    public List<OrderItemDTO> getOrderItems(String orderId) {
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

    // Order creation and processing - updated to accept Request DTOs with validation
    @Transactional
    public OrderDTO createOrder(OrderCreateRequest orderRequest) {
        // Validation is now handled in service layer
        if (orderRequest.getCustomerId() == null || orderRequest.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (orderRequest.getOrderItems() == null || orderRequest.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Validate each order item
        for (OrderItemCreateRequest item : orderRequest.getOrderItems()) {
            if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required for all order items");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for all order items");
            }
            if (item.getPrice() == null || item.getPrice() <= 0) {
                throw new IllegalArgumentException("Price must be positive for all order items");
            }
        }

        String customerId = orderRequest.getCustomerId();
        List<OrderItemCreateRequest> orderItemRequests = orderRequest.getOrderItems();

        // Convert OrderItemCreateRequest to OrderItemDTO for internal processing
        List<OrderItemDTO> orderItemDTOs = orderItemRequests.stream()
            .map(request -> new OrderItemDTO(null, request.getProductName(),
                                           request.getQuantity(), request.getPrice(), null))
            .toList();

        return createOrder(customerId, orderItemDTOs);
    }

    // Keep the existing createOrder method for backward compatibility
    @Transactional
    public OrderDTO createOrder(String customerId, List<OrderItemDTO> orderItemDTOs) {
        // Use optimized customer query if available
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
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
                throw new InsufficientStockException("Insufficient stock for product: " + item.getProductName());
            }

            totalAmount += item.getPrice() * item.getQuantity();
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Update product stock and sales count
        for (OrderItem item : orderItems) {
            String productId = getProductIdFromName(item.getProductName());
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
    public OrderDTO updateOrderStatus(String orderId, String newStatus) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(newStatus);
            Order savedOrder = orderRepository.save(order);
            return dtoMapper.toOrderDTO(savedOrder);
        }
        throw new OrderNotFoundException(orderId);
    }

    public OrderDTO processOrder(String orderId) {
        return updateOrderStatus(orderId, "PROCESSING");
    }

    public OrderDTO shipOrder(String orderId) {
        return updateOrderStatus(orderId, "SHIPPED");
    }

    public OrderDTO deliverOrder(String orderId) {
        return updateOrderStatus(orderId, "DELIVERED");
    }

    public OrderDTO cancelOrder(String orderId) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only cancel if order is still pending or processing
            if ("PENDING".equals(order.getStatus()) || "PROCESSING".equals(order.getStatus())) {
                // Restore product stock
                for (OrderItem item : order.getOrderItems()) {
                    String productId = getProductIdFromName(item.getProductName());
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
                throw new InvalidOrderStatusException(order.getStatus(), "cancel");
            }
        }
        throw new OrderNotFoundException(orderId);
    }

    // Order item operations - updated to accept Request DTOs with validation
    public OrderItemDTO addOrderItem(String orderId, OrderItemCreateRequest orderItemRequest) {
        // Validation for order item
        if (orderItemRequest.getProductName() == null || orderItemRequest.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (orderItemRequest.getQuantity() == null || orderItemRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (orderItemRequest.getPrice() == null || orderItemRequest.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Convert to OrderItemDTO for internal processing
        OrderItemDTO orderItemDTO = new OrderItemDTO(null, orderItemRequest.getProductName(),
                                                    orderItemRequest.getQuantity(), orderItemRequest.getPrice(), orderId);
        return addOrderItem(orderId, orderItemDTO);
    }

    // Keep the existing method for backward compatibility
    public OrderItemDTO addOrderItem(String orderId, OrderItemDTO orderItemDTO) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only add items to pending orders
            if (!"PENDING".equals(order.getStatus())) {
                throw new InvalidOrderStatusException(order.getStatus(), "add items to");
            }

            OrderItem orderItem = dtoMapper.toOrderItemEntity(orderItemDTO);
            orderItem.setOrder(order);
            OrderItem savedItem = orderItemRepository.save(orderItem);

            // Update order total
            order.setTotalAmount(order.getTotalAmount() + (orderItem.getPrice() * orderItem.getQuantity()));
            orderRepository.save(order);

            return dtoMapper.toOrderItemDTO(savedItem);
        }
        throw new OrderNotFoundException(orderId);
    }

    // Order calculations - using optimized queries
    public Double calculateOrderTotal(String orderId) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(orderId);
        if (order.isPresent()) {
            return order.get().getOrderItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();
        }
        return 0.0;
    }

    public Integer getOrderItemCount(String orderId) {
        Optional<Order> order = orderRepository.findByIdWithOrderItems(orderId);
        return order.map(o -> o.getOrderItems().size()).orElse(0);
    }

    // Utility methods
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getProductIdFromName(String productName) {
        Optional<Product> product = productRepository.findByName(productName);
        if (product.isPresent()) {
            return product.get().getId();
        }
        throw new ProductNotFoundException(productName, true);
    }

    // Order validation - using optimized queries
    public boolean canProcessOrder(String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isPresent() && "PENDING".equals(order.get().getStatus());
    }

    public boolean canCancelOrder(String orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()) {
            String status = order.get().getStatus();
            return "PENDING".equals(status) || "PROCESSING".equals(status);
        }
        return false;
    }

    // Update order - updated to accept Request DTOs with validation
    public OrderDTO updateOrder(String id, OrderUpdateRequest orderRequest) {
        // Validation for update request
        if (orderRequest.getStatus() == null || orderRequest.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Order status cannot be empty");
        }
        if (orderRequest.getTotalAmount() != null && orderRequest.getTotalAmount() < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative");
        }

        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only update if order is still pending
            if (!"PENDING".equals(order.getStatus())) {
                throw new InvalidOrderStatusException(order.getStatus(), "update");
            }

            order.setStatus(orderRequest.getStatus());
            if (orderRequest.getTotalAmount() != null) {
                order.setTotalAmount(orderRequest.getTotalAmount());
            }
            Order savedOrder = orderRepository.save(order);
            return dtoMapper.toOrderDTO(savedOrder);
        }
        throw new OrderNotFoundException(id);
    }

    // Keep the existing method for backward compatibility
    public OrderDTO updateOrder(String id, OrderDTO updatedOrderDTO) {
        Optional<Order> orderOpt = orderRepository.findByIdWithOrderItems(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();

            // Only update if order is still pending
            if (!"PENDING".equals(order.getStatus())) {
                throw new InvalidOrderStatusException(order.getStatus(), "update");
            }

            order.setStatus(updatedOrderDTO.getStatus());
            if (updatedOrderDTO.getTotalAmount() != null) {
                order.setTotalAmount(updatedOrderDTO.getTotalAmount());
            }
            Order savedOrder = orderRepository.save(order);
            return dtoMapper.toOrderDTO(savedOrder);
        }
        throw new OrderNotFoundException(id);
    }

    // Add new methods to leverage specific repository queries
    public List<OrderDTO> getCompletedOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findCompletedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getPendingOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findPendingOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCancelledOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findCancelledOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getShippedOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findShippedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getDeliveredOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findDeliveredOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getReturnedOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findReturnedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getRefundedOrdersByCustomer(String customerId) {
        List<Order> orders = orderRepository.findRefundedOrdersByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getOrdersAboveAmount(Double amount) {
        List<Order> orders = orderRepository.findByTotalAmountGreaterThan(amount);
        return dtoMapper.toOrderDTOList(orders);
    }
}
