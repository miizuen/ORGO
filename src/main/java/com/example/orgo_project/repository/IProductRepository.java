package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Product;
import com.example.orgo_project.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findBySellerId(Integer sellerId, Pageable pageable);

    Page<Product> findBySellerIdAndStatus(Integer sellerId, ProductStatus status, Pageable pageable);

    List<Product> findTop8ByStatusOrderByAverageRatingDesc(ProductStatus status);

    Page<Product> findByCategoryIdAndStatus(Integer categoryId, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = com.example.orgo_project.enums.ProductStatus.ACTIVE AND " +
           "(:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId)")
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                  @Param("categoryId") Integer categoryId,
                                  Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = com.example.orgo_project.enums.ProductStatus.ACTIVE AND " +
           "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findTop5ByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
