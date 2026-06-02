package com.example.orgo_project.repository;

import com.example.orgo_project.entity.CustomerOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICustomerOrderItemRepository extends JpaRepository<CustomerOrderItem, Integer> {
    List<CustomerOrderItem> findByOrderId(Integer orderId);

    List<CustomerOrderItem> findByOrderIdIn(List<Integer> orderIds);

    // Kiểm tra user đã mua sản phẩm (qua variant) với đơn hàng DELIVERED chưa
    @Query("SELECT COUNT(i) > 0 FROM CustomerOrderItem i " +
           "JOIN CustomerOrder o ON i.orderId = o.id " +
           "JOIN ProductVariant v ON i.productVariantId = v.id " +
           "WHERE o.userId = :userId AND v.productId = :productId " +
           "AND o.orderStatus = com.example.orgo_project.enums.OrderStatus.DELIVERED")
    boolean existsByUserIdAndProductId(@Param("userId") Integer userId,
                                       @Param("productId") Integer productId);
}
