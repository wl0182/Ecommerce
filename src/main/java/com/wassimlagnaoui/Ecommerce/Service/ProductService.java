package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Category;
import com.wassimlagnaoui.Ecommerce.Domain.Product;
import com.wassimlagnaoui.Ecommerce.Domain.Review;
import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Exception.CategoryNotFoundException;
import com.wassimlagnaoui.Ecommerce.Exception.InsufficientStockException;
import com.wassimlagnaoui.Ecommerce.Exception.ProductNotFoundException;
import com.wassimlagnaoui.Ecommerce.Repository.CategoryRepository;
import com.wassimlagnaoui.Ecommerce.Repository.ProductRepository;
import com.wassimlagnaoui.Ecommerce.Repository.ReviewRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
    @Cacheable(value = "products", cacheManager = "productCacheManager")
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return dtoMapper.toProductDTOList(products);
    }

    public Page<ProductDTO> getAllProductsPaginated(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(dtoMapper::toProductDTO);
    }

    @Cacheable(value = "product", key = "#id", cacheManager = "productCacheManager")
    public Optional<ProductDTO> getProductById(String id) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(dtoMapper::toProductDTO);
    }

    // Updated to accept Request DTOs with built-in validation
    @Caching(evict = {
        @CacheEvict(value = "products", cacheManager = "productCacheManager"),
        @CacheEvict(value = "product", key = "#result.id", cacheManager = "productCacheManager", condition = "#result != null")
    })
    public ProductDTO saveProduct(ProductCreateRequest productRequest) {
        // Validation is now handled in service layer
        if (!validateProduct(productRequest)) {
            throw new IllegalArgumentException("Invalid product data");
        }
        Product product = dtoMapper.toProductEntity(productRequest);
        Product savedProduct = productRepository.save(product);
        return dtoMapper.toProductDTO(savedProduct);
    }

    // Keep the old method for backward compatibility
    @Caching(evict = {
        @CacheEvict(value = "products", cacheManager = "productCacheManager"),
        @CacheEvict(value = "product", key = "#result.id", cacheManager = "productCacheManager", condition = "#result != null")
    })
    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product = dtoMapper.toProductEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        return dtoMapper.toProductDTO(savedProduct);
    }

    @Caching(evict = {
        @CacheEvict(value = "products", cacheManager = "productCacheManager"),
        @CacheEvict(value = "product", key = "#id", cacheManager = "productCacheManager")
    })
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    // Product search and filtering - now returning DTOs
    @Cacheable(value = "product", key = "'name:' + #name", cacheManager = "productCacheManager")
    public Optional<ProductDTO> findByName(String name) {
        Optional<Product> product = productRepository.findByName(name);
        return product.map(dtoMapper::toProductDTO);
    }

    @Cacheable(value = "products", key = "'search:' + #keyword", cacheManager = "productCacheManager")
    public List<ProductDTO> searchByKeyword(String keyword) {
        List<Product> products = productRepository.searchByKeyword(keyword);
        return dtoMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "'priceRange:' + #minPrice + '-' + #maxPrice", cacheManager = "productCacheManager")
    public List<ProductDTO> findByPriceRange(Double minPrice, Double maxPrice) {
        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return dtoMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "'category:' + #categoryName", cacheManager = "productCacheManager")
    public List<ProductDTO> findByCategoryName(String categoryName) {
        List<Product> products = productRepository.findByCategoryName(categoryName);
        return dtoMapper.toProductDTOList(products);
    }

    // Stock management - now returning DTOs
    @Cacheable(value = "products", key = "'inStock'", cacheManager = "productCacheManager")
    public List<ProductDTO> getProductsInStock() {
        List<Product> products = productRepository.findByStockGreaterThan(0);
        return dtoMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "'outOfStock'", cacheManager = "productCacheManager")
    public List<ProductDTO> getOutOfStockProducts() {
        List<Product> products = productRepository.findByStockLessThan(1);
        return dtoMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "'lowStock:' + #threshold", cacheManager = "productCacheManager")
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findProductsWithLowStock(threshold);
        return dtoMapper.toProductDTOList(products);
    }

    @Cacheable(value = "products", key = "'topStock'", cacheManager = "productCacheManager")
    public List<ProductDTO> getTopStockProducts() {
        List<Product> products = productRepository.findTop5ByOrderByStockDesc();
        return dtoMapper.toProductDTOList(products);
    }

    // Sales and popularity - now returning DTOs
    @Cacheable(value = "products", key = "'topSelling'", cacheManager = "productCacheManager")
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
    @Caching(
        put = @CachePut(value = "product", key = "#productId", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'inStock'", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'outOfStock'", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'topStock'", cacheManager = "productCacheManager")
        }
    )
    public ProductDTO updateStock(String productId, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new ProductNotFoundException(productId);
    }

    @Caching(
        put = @CachePut(value = "product", key = "#productId", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'inStock'", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'outOfStock'", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'topStock'", cacheManager = "productCacheManager")
        }
    )
    public ProductDTO reduceStock(String productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                Product savedProduct = productRepository.save(product);
                return dtoMapper.toProductDTO(savedProduct);
            }
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }
        throw new ProductNotFoundException(productId);
    }

    @Caching(
        put = @CachePut(value = "product", key = "#productId", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", key = "'topSelling'", cacheManager = "productCacheManager")
        }
    )
    public ProductDTO increaseSalesCount(String productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer currentSales = product.getSalesCount() != null ? product.getSalesCount() : 0;
            product.setSalesCount(currentSales + quantity);
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new ProductNotFoundException(productId);
    }

    // Review management - now returning DTOs
    @Cacheable(value = "product", key = "'reviews:' + #productId", cacheManager = "productCacheManager")
    public List<ReviewDTO> getProductReviews(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        return dtoMapper.toReviewDTOList(reviews);
    }

    @Cacheable(value = "product", key = "'avgRating:' + #productId", cacheManager = "productCacheManager")
    public Double getProductAverageRating(String productId) {
        return reviewRepository.findAverageRatingByProductId(productId);
    }

    @Cacheable(value = "product", key = "'reviewCount:' + #productId", cacheManager = "productCacheManager")
    public Long getProductReviewCount(String productId) {
        return reviewRepository.countReviewsByProductId(productId);
    }

    @Cacheable(value = "product", key = "'reviewsByRating:' + #productId + ':' + #minRating", cacheManager = "productCacheManager")
    public List<ReviewDTO> getProductReviewsByRating(String productId, Integer minRating) {
        List<Review> productReviews = reviewRepository.findByProductId(productId);
        List<Review> filteredReviews = productReviews.stream()
                .filter(review -> review.getRating() >= minRating)
                .toList();
        return dtoMapper.toReviewDTOList(filteredReviews);
    }

    // Get product with rating summary
    @Cacheable(value = "product", key = "'summary:' + #productId", cacheManager = "productCacheManager")
    public ProductSummaryDTO getProductSummary(String productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
            Long reviewCount = reviewRepository.countReviewsByProductId(productId);
            return dtoMapper.toProductSummaryDTO(product, avgRating, reviewCount);
        }
        throw new ProductNotFoundException(productId);
    }

    // Category management - now returning DTOs
    @Caching(
        put = @CachePut(value = "product", key = "#productId", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", cacheManager = "productCacheManager"),
            @CacheEvict(value = "products", key = "'category:' + #categoryName", cacheManager = "productCacheManager")
        }
    )
    public ProductDTO addCategoryToProduct(String productId, String categoryName) {
        Optional<Product> productOpt = productRepository.findById(productId);
        Optional<Category> categoryOpt = categoryRepository.findByName(categoryName);

        if (productOpt.isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
        if (categoryOpt.isEmpty()) {
            throw new CategoryNotFoundException(categoryName);
        }

        Product product = productOpt.get();
        product.getCategories().add(categoryOpt.get());
        Product savedProduct = productRepository.save(product);
        return dtoMapper.toProductDTO(savedProduct);
    }

    // Product validation and business logic
    @Cacheable(value = "product", key = "'exists:' + #name", cacheManager = "productCacheManager")
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Cacheable(value = "product", key = "'available:' + #productId + ':' + #requestedQuantity", cacheManager = "productCacheManager")
    public boolean isProductAvailable(String productId, Integer requestedQuantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        return productOpt.isPresent() && productOpt.get().getStock() >= requestedQuantity;
    }

    // Update product - updated to accept Request DTOs with validation
    @Caching(
        put = @CachePut(value = "product", key = "#id", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", cacheManager = "productCacheManager"),
            @CacheEvict(value = "product", key = "'exists:' + #productRequest.name", cacheManager = "productCacheManager", condition = "#productRequest.name != null")
        }
    )
    public ProductDTO updateProduct(String id, ProductUpdateRequest productRequest) {
        // Validation for update request
        if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (productRequest.getDescription() == null || productRequest.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
        if (productRequest.getPrice() == null || productRequest.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be positive");
        }
        if (productRequest.getStock() != null && productRequest.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }

        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();

            // Check if name is already taken by another product
            if (!product.getName().equals(productRequest.getName()) && existsByName(productRequest.getName())) {
                throw new IllegalArgumentException("Product name already exists");
            }

            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            if (productRequest.getStock() != null) {
                product.setStock(productRequest.getStock());
            }
            if (productRequest.getSalesCount() != null) {
                product.setSalesCount(productRequest.getSalesCount());
            }
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new ProductNotFoundException(id);
    }

    // Keep the old method for backward compatibility
    @Caching(
        put = @CachePut(value = "product", key = "#id", cacheManager = "productCacheManager"),
        evict = {
            @CacheEvict(value = "products", cacheManager = "productCacheManager"),
            @CacheEvict(value = "product", key = "'exists:' + #updatedProductDTO.name", cacheManager = "productCacheManager", condition = "#updatedProductDTO.name != null")
        }
    )
    public ProductDTO updateProduct(String id, ProductDTO updatedProductDTO) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setName(updatedProductDTO.getName());
            product.setDescription(updatedProductDTO.getDescription());
            product.setPrice(updatedProductDTO.getPrice());
            if (updatedProductDTO.getStock() != null) {
                product.setStock(updatedProductDTO.getStock());
            }
            if (updatedProductDTO.getSalesCount() != null) {
                product.setSalesCount(updatedProductDTO.getSalesCount());
            }
            Product savedProduct = productRepository.save(product);
            return dtoMapper.toProductDTO(savedProduct);
        }
        throw new ProductNotFoundException(id);
    }

    // Product validation - updated to accept Request DTOs
    public boolean validateProduct(@Valid ProductValidationRequest productRequest) {
        if (productRequest.getName() == null || productRequest.getName().trim().isEmpty()) {
            return false;
        }
        if (productRequest.getPrice() == null || productRequest.getPrice() <= 0) {
            return false;
        }
        if (productRequest.getStock() == null || productRequest.getStock() < 0) {
            return false;
        }
        return !existsByName(productRequest.getName());
    }

    // Keep the old method for backward compatibility
    public boolean validateProduct(ProductCreateRequest productDTO) {
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
