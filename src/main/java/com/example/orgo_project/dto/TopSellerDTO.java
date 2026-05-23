package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopSellerDTO {
    private Integer sellerId;
    private String shopName;
    private BigDecimal totalRevenue;
    private Integer totalOrders;
    private Double avgRating;
    private String status;
}
