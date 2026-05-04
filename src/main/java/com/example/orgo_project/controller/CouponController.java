package com.example.orgo_project.controller;

import com.example.orgo_project.dto.CouponValidateRequestDTO;
import com.example.orgo_project.dto.CouponValidateResponseDTO;
import com.example.orgo_project.service.ICouponService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    private final ICouponService couponService;

    public CouponController(ICouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidateResponseDTO> validateCoupon(@RequestBody CouponValidateRequestDTO request) {
        return ResponseEntity.ok(couponService.validateCoupon(request));
    }
}