package com.example.orgo_project.service;

import com.example.orgo_project.entity.EscrowBalance;

import java.math.BigDecimal;
import java.util.Map;

public interface EscrowService {
    EscrowBalance createEscrowForOrder(Integer orderId, BigDecimal totalAmount, BigDecimal commissionAmount, BigDecimal sellerPayoutAmount, BigDecimal adminRevenueAmount);
    EscrowBalance findByOrderId(Integer orderId);
    java.util.List<EscrowBalance> findAll();
    BigDecimal getTotalHeldAmount();
    BigDecimal getTotalAdminRevenue();
    java.util.List<com.example.orgo_project.entity.OrderSettlement> findAllSettlements();
    EscrowBalance settleOrder(Integer orderId, Map<Integer, BigDecimal> sellerTotals);
}
