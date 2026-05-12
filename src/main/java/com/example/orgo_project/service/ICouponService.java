package com.example.orgo_project.service;

import com.example.orgo_project.dto.CouponValidateRequestDTO;
import com.example.orgo_project.dto.CouponValidateResponseDTO;

public interface ICouponService {
    CouponValidateResponseDTO validateCoupon(CouponValidateRequestDTO request);
}