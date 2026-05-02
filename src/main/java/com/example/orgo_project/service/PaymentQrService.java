package com.example.orgo_project.service;

import com.example.orgo_project.entity.PaymentQrSession;

import java.math.BigDecimal;

public interface PaymentQrService {
    PaymentQrSession createQrSession(Integer orderId, BigDecimal amount);
    PaymentQrSession findByOrderId(Integer orderId);
}
