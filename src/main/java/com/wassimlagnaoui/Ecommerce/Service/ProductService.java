package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Category;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.Domain.Review;
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

    // Basic CRUD operations
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProductsPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Product search and filtering
    public Optional<Product> findByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> searchByKeyword(String keyword) {
        return productRepository.searchByKeyword(keyword);
    }

    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> findByCategoryName(String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    // Stock management
    public List<Product> getProductsInStock() {
        return productRepository.findByStockGreaterThan(0);
    }

    public List<Product> getOutOfStockProducts() {
        return productRepository.findByStockLessThan(1);
    }

    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findProductsWithLowStock(threshold);
    }

    public List<Product> getTopStockProducts() {
        return productRepository.findTop5ByOrderByStockDesc();
    }

    // Sales and popularity
    public List<Product> getTopSellingProducts() {
        return productRepository.findTopSellingProducts();
    }

    // Stock operations
    public Product updateStock(Long productId, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    public Product reduceStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                return productRepository.save(product);
            }
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    public Product increaseSalesCount(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer currentSales = product.getSalesCount() != null ? product.getSalesCount() : 0;
            product.setSalesCount(currentSales + quantity);
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + productId);
    }

    // Review management
    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Double getProductAverageRating(Long productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    public Long getProductReviewCount(Long productId) {
        return reviewRepository.countReviewsByProductId(productId);
    }

    public List<Review> getProductReviewsByRating(Long productId, Integer minRating) {
        List<Review> productReviews = reviewRepository.findByProductId(productId);
        return productReviews.stream()
                .filter(review -> review.getRating() >= minRating)
                .toList();
    }

    // Category management
    public Product addCategoryToProduct(Long productId, String categoryName) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);

        if (productOpt.isPresent() && categoryOpt.isPresent()) {
            Product product = productOpt.get();
            product.getCategories().add(categoryOpt.get());
            return productRepository.save(product);
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

    public Product updateProduct(Long id, Product updatedProduct) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            if (updatedProduct.getStock() != null) {
                product.setStock(updatedProduct.getStock());
            }
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    // Product validation
    public boolean validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            return false;
        }
        if (product.getPrice() == null || product.getPrice() <= 0) {
            return false;
        }
        if (product.getStock() == null || product.getStock() < 0) {
            return false;
        }
        return !existsByName(product.getName());
    }
}
