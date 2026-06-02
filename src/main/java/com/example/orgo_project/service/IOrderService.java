package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.enums.OrderStatus;

import java.util.List;

public interface IOrderService {
    List<OrderSummaryDTO> getMyOrders(Integer accountId);
    
    // ✅ Thêm method còn thiếu
    List<OrderSummaryDTO> getMyOrdersByStatus(Integer accountId, OrderStatus status);

    OrderDetailDTO getOrderDetail(Integer accountId, Integer orderId);

    boolean cancelOrder(Integer accountId, Integer orderId, String reason, String refundBankName, String refundAccountNumber, String refundAccountName);
    
    // ✅ Thêm method còn thiếu
    boolean confirmDelivery(Integer accountId, Integer orderId);
}
