package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Category;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.Domain.Review;
import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Repository.CategoryRepository;
import com.wassimlagnaoui.Ecommerce.Repository.ProductRepository;
import com.wassimlagnaoui.Ecommerce.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DTOMapper dtoMapper;

    // Basic CRUD operations - now returning DTOs
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return dtoMapper.toProductDTOList(products);
    }

    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(dtoMapper::toProductDTO);
    }

    public Optional<ProductDTO> getProductById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(dtoMapper::toProductDTO);
    }

    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product = dtoMapper.toProductEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return dtoMapper.toProductDTO(savedProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Product search and filtering - now returning DTOs
    public Optional<ProductDTO> findByName(String name) {
        Optional<Product> product = productRepository.findByName(name);
        return product.map(dtoMapper::toProductDTO);
    }

    public List<ProductDTO> searchByKeyword(String keyword) {
        List<Product> products = productRepository.searchByKeyword(keyword);
        return dtoMapper.toProductDTOList(products);
    }

    public List<ProductDTO> findByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return dtoMapper.toProductDTOList(products);
    }

    public List<ProductDTO> findByCategoryName(String categoryName) {
        List<Product> products = productRepository.findByCategoryName(categoryName);
        return dtoMapper.toProductDTOList(products);
    }

    // Stock management - now returning DTOs
    public List<ProductDTO> getProductsInStock() {
        List<Product> products = productRepository.findByStockGreaterThan(0);
        return dtoMapper.toProductDTOList(products);
    }

    public List<ProductDTO> getOutOfStockProducts() {
        List<Product> products = productRepository.findByStockLessThan(1);
        return dtoMapper.toProductDTOList(products);
    }

    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findProductsWithLowStock(threshold);
        return dtoMapper.toProductDTOList(products);
    }

    public List<ProductDTO> getTopStockProducts() {
        List<Product> products = productRepository.findTop5ByOrderByStockDesc();
        return dtoMapper.toProductDTOList(products);
    }

    // Sales and popularity - now returning DTOs
    public List<ProductSummaryDTO> getTopSellingProducts() {
        List<Product> products = productRepository.findTopSellingProducts();
        return products.stream()
                .map(product -> {
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
                    Long reviewCount = reviewRepository.countReviewsByProductId(product.getId());
                    return dtoMapper.toProductSummaryDTO(product, avgRating, reviewCount);
                })
                .toList();
    }

    // Stock operations - now returning DTOs
    public ProductDTO updateStock(Long productId, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    public ProductDTO reduceStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                Product savedProduct = productRepository.save(product);
                return dtoMapper.toProductDTO(savedProduct);
            }
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    public ProductDTO increaseSalesCount(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer currentSales = product.getSalesCount() != null ? product.getSalesCount() : 0;
            product.setSalesCount(currentSales + quantity);
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    // Review management - now returning DTOs
    public List<ReviewDTO> getProductReviews(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return dtoMapper.toReviewDTOList(reviews);
    }

    public Double getProductAverageRating(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    public Long getProductReviewCount(Long productId) {
        return reviewRepository.countReviewsByProductId(productId);
    }

    public List<ReviewDTO> getProductReviewsByRating(Long productId, Integer minRating) {
        List<Review> productReviews = reviewRepository.findByProductId(productId);
        List<Review> filteredReviews = productReviews.stream()
                .filter(review -> review.getRating() >= minRating)
                .toList();
        return dtoMapper.toReviewDTOList(filteredReviews);
    }

    // Get product with rating summary
    public ProductSummaryDTO getProductSummary(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
            Long reviewCount = reviewRepository.countReviewsByProductId(productId);
            return dtoMapper.toProductSummaryDTO(product, avgRating, reviewCount);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    // Category management - now returning DTOs
    public ProductDTO addCategoryToProduct(Long productId, String categoryName) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);

        if (productOpt.isPresent() && categoryOpt.isPresent()) {
            Product product = productOpt.get();
            product.getCategories().add(categoryOpt.get());
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new RuntimeException("Product or Category not found");
    }

    // Product validation and business logic
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    public boolean isProductAvailable(Long productId, Integer requestedQuantity) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() && product.get().getStock() >= requestedQuantity;
    }

    public ProductDTO updateProduct(Long id, ProductDTO updatedProductDTO) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(updatedProductDTO.getName());
            product.setDescription(updatedProductDTO.getDescription());
            product.setPrice(updatedProductDTO.getPrice());
            if (updatedProductDTO.getStock() != null) {
                product.setStock(updatedProductDTO.getStock());
            }
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    // Product validation - now using DTO
    public boolean validateProduct(ProductDTO productDTO) {
        if (productDTO.getName() == null || productDTO.getName().trim().isEmpty()) {
            return false;
        }
        if (productDTO.getPrice() == null || productDTO.getPrice() <= 0) {
            return false;
        }
        if (productDTO.getStock() == null || productDTO.getStock() < 0) {
            return false;
        }
        return !existsByName(productDTO.getName());
    }
}
