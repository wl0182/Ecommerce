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

    // Basic CRUD operations - now returning DTOs
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return dtoMapper.toOrderDTOList(orders);
    }

    public Optional<OrderDTO> getOrderById(Long id) {
        Optional<Order> order = orderRepository.findById(id);
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

    // Order search and filtering - now returning DTOs
    public Optional<OrderDTO> findByOrderNumber(String orderNumber) {
        Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
        return order.map(dtoMapper::toOrderDTO);
    }

    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
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

    // Order item management - now returning DTOs
    public List<OrderItemDTO> getOrderItems(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return dtoMapper.toOrderItemDTOList(orderItems);
    }

    public List<OrderItemDTO> findOrderItemsByProductName(String productName) {
        List<OrderItem> orderItems = orderItemRepository.findByProductName(productName);
        return dtoMapper.toOrderItemDTOList(orderItems);
    }

    // Order creation and processing - now returning DTOs
    @Transactional
    public OrderDTO createOrder(Long customerId, List<OrderItemDTO> orderItemDTOs) {
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

        return dtoMapper.toOrderDTO(savedOrder);
    }

    // Order status management - now returning DTOs
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
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
                Order savedOrder = orderRepository.save(order);
                return dtoMapper.toOrderDTO(savedOrder);
            } else {
                throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
            }
        }
        throw new RuntimeException("Order not found with id: " + orderId);
    }

    // Order item operations - now returning DTOs
    public OrderItemDTO addOrderItem(Long orderId, OrderItemDTO orderItemDTO) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
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

    // Update order - now using DTOs
    public OrderDTO updateOrder(Long id, OrderDTO updatedOrderDTO) {
        Optional<Order> orderOpt = orderRepository.findById(id);
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
}
