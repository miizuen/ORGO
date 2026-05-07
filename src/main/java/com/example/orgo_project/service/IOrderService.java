package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;

import java.util.List;

public interface IOrderService {
    List<OrderSummaryDTO> getMyOrders(Integer accountId);

    OrderDetailDTO getOrderDetail(Integer accountId, Integer orderId);

    boolean cancelOrder(Integer accountId, Integer orderId, String reason);

    boolean requestReturn(Integer accountId, ReturnRequestDTO request);
}