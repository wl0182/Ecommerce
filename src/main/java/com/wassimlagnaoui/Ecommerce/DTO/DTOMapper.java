package com.wassimlagnaoui.Ecommerce.DTO;

import com.wassimlagnaoui.Ecommerce.Domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DTOMapper {

    // Customer mappings
    public CustomerDTO toCustomerDTO(Customer customer) {
        if (customer == null) return null;
        return new CustomerDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getTotalSpent()
        );
    }

    public CustomerSummaryDTO toCustomerSummaryDTO(Customer customer) {
        if (customer == null) return null;
        int orderCount = customer.getOrders() != null ? customer.getOrders().size() : 0;
        return new CustomerSummaryDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getTotalSpent(),
                orderCount
        );
    }

    public Customer toCustomerEntity(CustomerDTO dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setTotalSpent(dto.getTotalSpent());
        return customer;
    }

    // Product mappings
    public ProductDTO toProductDTO(Product product) {
        if (product == null) return null;
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getSalesCount()
        );
    }

    public ProductSummaryDTO toProductSummaryDTO(Product product, Double averageRating, Long reviewCount) {
        if (product == null) return null;
        return new ProductSummaryDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                averageRating,
                reviewCount
        );
    }

    public Product toProductEntity(ProductDTO dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setSalesCount(dto.getSalesCount());
        return product;
    }

    // Order mappings
    public OrderDTO toOrderDTO(Order order) {
        if (order == null) return null;
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems() != null
            ? order.getOrderItems().stream().map(this::toOrderItemDTO).collect(Collectors.toList())
            : null;

        return new OrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCustomer() != null ? order.getCustomer().getId() : null,
                order.getCustomer() != null ? order.getCustomer().getName() : null,
                orderItemDTOs
        );
    }

    public Order toOrderEntity(OrderDTO dto) {
        if (dto == null) return null;
        Order order = new Order();
        order.setId(dto.getId());
        order.setOrderNumber(dto.getOrderNumber());
        order.setStatus(dto.getStatus());
        order.setTotalAmount(dto.getTotalAmount());
        return order;
    }

    // OrderItem mappings
    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) return null;
        return new OrderItemDTO(
                orderItem.getId(),
                orderItem.getProductName(),
                orderItem.getQuantity(),
                orderItem.getPrice(),
                orderItem.getOrder() != null ? orderItem.getOrder().getId() : null
        );
    }

    public OrderItem toOrderItemEntity(OrderItemDTO dto) {
        if (dto == null) return null;
        OrderItem orderItem = new OrderItem();
        orderItem.setId(dto.getId());
        orderItem.setProductName(dto.getProductName());
        orderItem.setQuantity(dto.getQuantity());
        orderItem.setPrice(dto.getPrice());
        return orderItem;
    }

    // Address mappings
    public AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        return new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getCountry(),
                address.getZipCode(),
                address.getCustomer() != null ? address.getCustomer().getId() : null
        );
    }

    public Address toAddressEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address address = new Address();
        address.setId(dto.getId());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setCountry(dto.getCountry());
        address.setZipCode(dto.getZipCode());
        return address;
    }

    // Review mappings
    public ReviewDTO toReviewDTO(Review review) {
        if (review == null) return null;
        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getProduct() != null ? review.getProduct().getId() : null,
                review.getProduct() != null ? review.getProduct().getName() : null,
                review.getCustomer() != null ? review.getCustomer().getId() : null,
                review.getCustomer() != null ? review.getCustomer().getName() : null
        );
    }

    public Review toReviewEntity(ReviewDTO dto) {
        if (dto == null) return null;
        Review review = new Review();
        review.setId(dto.getId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }

    // Category mappings
    public CategoryDTO toCategoryDTO(Category category) {
        if (category == null) return null;
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }

    public Category toCategoryEntity(CategoryDTO dto) {
        if (dto == null) return null;
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    // List mappings
    public List<CustomerDTO> toCustomerDTOList(List<Customer> customers) {
        return customers.stream().map(this::toCustomerDTO).collect(Collectors.toList());
    }

    public List<ProductDTO> toProductDTOList(List<Product> products) {
        return products.stream().map(this::toProductDTO).collect(Collectors.toList());
    }

    public List<OrderDTO> toOrderDTOList(List<Order> orders) {
        return orders.stream().map(this::toOrderDTO).collect(Collectors.toList());
    }

    public List<OrderItemDTO> toOrderItemDTOList(List<OrderItem> orderItems) {
        return orderItems.stream().map(this::toOrderItemDTO).collect(Collectors.toList());
    }

    public List<AddressDTO> toAddressDTOList(List<Address> addresses) {
        return addresses.stream().map(this::toAddressDTO).collect(Collectors.toList());
    }

    public List<ReviewDTO> toReviewDTOList(List<Review> reviews) {
        return reviews.stream().map(this::toReviewDTO).collect(Collectors.toList());
    }

    public List<CategoryDTO> toCategoryDTOList(List<Category> categories) {
        return categories.stream().map(this::toCategoryDTO).collect(Collectors.toList());
    }
}
