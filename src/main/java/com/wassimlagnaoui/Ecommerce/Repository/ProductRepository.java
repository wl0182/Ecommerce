package com.wassimlagnaoui.Ecommerce.Repository;

import com.wassimlagnaoui.Ecommerce.Domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByName(String name);
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    List<Product> findByStockGreaterThan(Integer stock);
    List<Product> findByStockLessThan(Integer stock);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    // Account for lazy loading of reviews to avoid N+1 problem
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews WHERE p.id = ?1")
    Optional<Product> findByIdWithReviews(Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews")
    List<Product> findAllWithReviews();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.categories WHERE p.id = ?1")
    Optional<Product> findByIdWithCategories(Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.categories")
    List<Product> findAllWithCategories();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews LEFT JOIN FETCH p.categories WHERE p.id = ?1")
    Optional<Product> findByIdWithAllDetails(Long id);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews LEFT JOIN FETCH p.categories")
    List<Product> findAllWithAllDetails();

    // add other important queries

    // products with average rating greater than a certain value
    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p HAVING AVG(r.rating) > :rating")
    List<Product> findByAverageRatingGreaterThan(@Param("rating") Double rating);

    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p HAVING AVG(r.rating) < :rating")
    List<Product> findByAverageRatingLessThan(@Param("rating") Double rating);

    @Query("SELECT p FROM Product p JOIN p.reviews r GROUP BY p HAVING AVG(r.rating) = :rating")
    List<Product> findByAverageRatingEquals(@Param("rating") Double rating);

    // find top 5 products by stock
    @Query("SELECT p FROM Product p ORDER BY p.stock DESC limit 5")
    List<Product> findTop5ByOrderByStockDesc();

    @Override
    Page<Product> findAll(Pageable pageable);

    boolean existsByName(String name);

    // find top-selling products
    @Query("SELECT p FROM Product p ORDER BY p.salesCount DESC")
    List<Product> findTopSellingProducts();

    // find products with low stock (less than a certain threshold)
    @Query("SELECT p FROM Product p WHERE p.stock < :threshold")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);



}
