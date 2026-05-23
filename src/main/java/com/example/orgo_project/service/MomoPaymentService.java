package com.example.orgo_project.service;

import com.example.orgo_project.dto.MomoCreateResponseDTO;
import com.example.orgo_project.dto.MomoIpnRequestDTO;

public interface MomoPaymentService {
    MomoCreateResponseDTO createPayment(Integer orderId, String orderCode, String orderInfo, long amount);

    boolean handleIpn(MomoIpnRequestDTO request);
}
