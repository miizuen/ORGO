package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Product;
import com.example.orgo_project.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findBySellerId(Integer sellerId);

    @Query("""
            select p
            from Product p
            where p.status = :status
            order by p.id desc
            """)
    Page<Product> findByStatus(@Param("status") ProductStatus status, Pageable pageable);

    @Query("""
            select p
            from Product p
            where p.categoryId = :categoryId
              and p.status = :status
            order by p.id desc
            """)
    Page<Product> findByCategoryIdAndStatus(@Param("categoryId") Integer categoryId,
                                            @Param("status") ProductStatus status,
                                            Pageable pageable);

    @Query("""
            select p
            from Product p
            where p.sellerId = :sellerId
            order by p.id desc
            """)
    Page<Product> findBySellerId(@Param("sellerId") Integer sellerId, Pageable pageable);

    @Query("""
            select p
            from Product p
            where (:keyword is null or lower(p.productName) like lower(concat('%', :keyword, '%')))
              and (:categoryId is null or p.categoryId = :categoryId)
            order by p.id desc
            """)
    Page<Product> searchProducts(@Param("keyword") String keyword,
                                 @Param("categoryId") Integer categoryId,
                                 Pageable pageable);

    @Query(value = "SELECT TOP 4 p.* FROM SanPham p " +
            "WHERE p.trang_thai = 'ACTIVE' " +
            "ORDER BY NEWID()", nativeQuery = true)
    List<Product> findRandomTop4ActiveProducts();

    @Query(value = "SELECT TOP 4 p.* FROM SanPham p " +
            "WHERE p.trang_thai = 'ACTIVE' AND p.id_san_pham NOT IN (:excludedIds) " +
            "ORDER BY NEWID()", nativeQuery = true)
    List<Product> findRandomTop4ActiveProductsExcluding(@Param("excludedIds") java.util.Set<Integer> excludedIds);

    @Query("SELECT p FROM Product p WHERE p.status = com.example.orgo_project.enums.ProductStatus.ACTIVE AND " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findTop5ByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            select p
            from Product p
            where p.status = :status
            order by coalesce(p.averageRating, 0) desc
            """)
    List<Product> findTop8ByStatusOrderByAverageRatingDesc(@Param("status") ProductStatus status);
}