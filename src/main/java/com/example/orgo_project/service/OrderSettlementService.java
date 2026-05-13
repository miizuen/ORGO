package com.example.orgo_project.service;

import com.example.orgo_project.entity.OrderSettlement;

public interface OrderSettlementService {
    OrderSettlement settleOrder(Integer orderId);
}
