package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;

import java.util.List;

public interface IAdminOrderService {
    List<OrderSummaryDTO> getAllOrders();

    OrderDetailDTO getOrderDetail(Integer orderId);

    boolean approveOrder(Integer orderId);
}
