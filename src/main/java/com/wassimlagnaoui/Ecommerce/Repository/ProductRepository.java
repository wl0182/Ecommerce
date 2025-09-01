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
