package com.example.orgo_project.repository;

import com.example.orgo_project.entity.OrderSettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderSettlementRepository extends JpaRepository<OrderSettlement, Integer> {
    List<OrderSettlement> findByOrderId(Integer orderId);
    List<OrderSettlement> findBySellerId(Integer sellerId);
}
