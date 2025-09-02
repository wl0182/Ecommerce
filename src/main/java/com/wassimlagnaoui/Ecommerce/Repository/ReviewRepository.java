package com.wassimlagnaoui.Ecommerce.Repository;

import com.wassimlagnaoui.Ecommerce.Domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByProductId(String productId);
    List<Review> findByCustomerId(String customerId);
    List<Review> findByRating(Integer rating);
    List<Review> findByRatingGreaterThanEqual(Integer rating);
    List<Review> findByProductIdAndCustomerId(String productId, String customerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") String productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") String productId);
}
