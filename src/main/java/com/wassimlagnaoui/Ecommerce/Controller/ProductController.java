package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Basic CRUD operations - now using DTOs
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductDTO>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> products = productService.getAllProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<ProductDTO> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<ProductSummaryDTO> getProductSummary(@PathVariable Long id) {
        try {
            ProductSummaryDTO productSummary = productService.getProductSummary(id);
            return ResponseEntity.ok(productSummary);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO product) {
        try {
            if (!productService.validateProduct(product)) {
                return ResponseEntity.badRequest().build();
            }
            ProductDTO savedProduct = productService.saveProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
        try {
            ProductDTO updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Product search and filtering - now using DTOs
    @GetMapping("/name/{name}")
    public ResponseEntity<ProductDTO> getProductByName(@PathVariable String name) {
        Optional<ProductDTO> product = productService.findByName(name);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchByKeyword(keyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        List<ProductDTO> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable String categoryName) {
        List<ProductDTO> products = productService.findByCategoryName(categoryName);
        return ResponseEntity.ok(products);
    }

    // Stock management endpoints - now using DTOs
    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductDTO>> getProductsInStock() {
        List<ProductDTO> products = productService.getProductsInStock();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<List<ProductDTO>> getOutOfStockProducts() {
        List<ProductDTO> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(@RequestParam Integer threshold) {
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top-stock")
    public ResponseEntity<List<ProductDTO>> getTopStockProducts() {
        List<ProductDTO> products = productService.getTopStockProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductSummaryDTO>> getTopSellingProducts() {
        List<ProductSummaryDTO> products = productService.getTopSellingProducts();
        return ResponseEntity.ok(products);
    }

    // Stock operations - now using DTOs
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductDTO> updateStock(@PathVariable Long productId, @RequestParam Integer newStock) {
        try {
            ProductDTO updatedProduct = productService.updateStock(productId, newStock);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{productId}/reduce-stock")
    public ResponseEntity<ProductDTO> reduceStock(@PathVariable Long productId, @RequestParam Integer quantity) {
        try {
            ProductDTO updatedProduct = productService.reduceStock(productId, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{productId}/increase-sales")
    public ResponseEntity<ProductDTO> increaseSalesCount(@PathVariable Long productId, @RequestParam Integer quantity) {
        try {
            ProductDTO updatedProduct = productService.increaseSalesCount(productId, quantity);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Review management endpoints - now using DTOs
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        List<ReviewDTO> reviews = productService.getProductReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{productId}/average-rating")
    public ResponseEntity<Double> getProductAverageRating(@PathVariable Long productId) {
        Double averageRating = productService.getProductAverageRating(productId);
        return ResponseEntity.ok(averageRating);
    }

    @GetMapping("/{productId}/review-count")
    public ResponseEntity<Long> getProductReviewCount(@PathVariable Long productId) {
        Long reviewCount = productService.getProductReviewCount(productId);
        return ResponseEntity.ok(reviewCount);
    }

    @GetMapping("/{productId}/reviews/rating/{minRating}")
    public ResponseEntity<List<ReviewDTO>> getProductReviewsByRating(
            @PathVariable Long productId,
            @PathVariable Integer minRating) {
        List<ReviewDTO> reviews = productService.getProductReviewsByRating(productId, minRating);
        return ResponseEntity.ok(reviews);
    }

    // Category management - now using DTOs
    @PutMapping("/{productId}/categories/{categoryName}")
    public ResponseEntity<ProductDTO> addCategoryToProduct(
            @PathVariable Long productId,
            @PathVariable String categoryName) {
        try {
            ProductDTO updatedProduct = productService.addCategoryToProduct(productId, categoryName);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Product validation and availability
    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> checkProductNameExists(@PathVariable String name) {
        boolean exists = productService.existsByName(name);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{productId}/availability")
    public ResponseEntity<Boolean> checkProductAvailability(
            @PathVariable Long productId,
            @RequestParam Integer requestedQuantity) {
        boolean available = productService.isProductAvailable(productId, requestedQuantity);
        return ResponseEntity.ok(available);
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateProduct(@RequestBody ProductDTO product) {
        boolean isValid = productService.validateProduct(product);
        return ResponseEntity.ok(isValid);
    }
}
