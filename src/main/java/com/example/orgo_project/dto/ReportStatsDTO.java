package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStatsDTO {
    // Revenue Stats
    private BigDecimal totalRevenue;
    private BigDecimal totalCommission;
    private BigDecimal totalRefunds;
    private Integer totalOrders;
    private Double revenueGrowth;
    
    // Seller Stats
    private Integer totalSellers;
    private Integer activeSellers;
    private Double avgDeliveryTime;
    private Double onTimeDeliveryRate;
    private Double returnRate;
    
    // Customer Stats
    private Integer totalUsers;
    private Integer newUsers;
    private Integer activeUsers;
    private Double conversionRate;
    
    // Charts Data
    private List<String> revenueLabels;
    private List<BigDecimal> revenueData;
    private List<BigDecimal> orderCountData;
    
    private Map<String, BigDecimal> categoryRevenue;
    private Map<String, Integer> paymentMethods;
    
    // Top Sellers
    private List<TopSellerDTO> topSellers;
}
