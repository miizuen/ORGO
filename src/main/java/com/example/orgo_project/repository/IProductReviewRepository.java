package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductReviewRepository extends JpaRepository<ProductReview, Integer> {

    Page<ProductReview> findByProductId(Integer productId, Pageable pageable);

    boolean existsByProductIdAndUserId(Integer productId, Integer userId);

    @Query("SELECT AVG(r.stars) FROM ProductReview r WHERE r.productId = :productId")
    Double findAverageRatingByProductId(@Param("productId") Integer productId);

    long countByProductId(Integer productId);
}
