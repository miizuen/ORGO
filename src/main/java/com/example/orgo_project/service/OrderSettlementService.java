package com.example.orgo_project.service;

import com.example.orgo_project.entity.EscrowBalance;

public interface OrderSettlementService {
    EscrowBalance settleOrder(Integer orderId);
}
