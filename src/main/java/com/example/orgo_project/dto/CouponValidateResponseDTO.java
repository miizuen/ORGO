package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidateResponseDTO {
    private boolean valid;
    private String message;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}