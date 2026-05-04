package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;

import java.util.List;

public interface ISellerOrderService {
    List<OrderSummaryDTO> getSellerOrders(Integer sellerAccountId);

    OrderDetailDTO getSellerOrderDetail(Integer sellerAccountId, Integer orderId);

    boolean confirmOrder(Integer sellerAccountId, Integer orderId);

    boolean shipOrder(Integer sellerAccountId, Integer orderId);

    boolean deliverOrder(Integer sellerAccountId, Integer orderId);
}