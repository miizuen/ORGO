package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;

import java.util.List;

public interface IAdminOrderService {
    List<OrderSummaryDTO> getAllOrders();

    OrderDetailDTO getOrderDetail(Integer orderId);

    List<ReturnRequestDTO> getReturnRequests();

    boolean approveReturn(Integer returnId);

    boolean rejectReturn(Integer returnId);
}