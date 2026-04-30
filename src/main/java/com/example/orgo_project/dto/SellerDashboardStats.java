package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDashboardStats {
    private Long totalProducts;
    private Long totalOrders;
    private BigDecimal revenue;
    private Map<String, Long> ordersByStatus;
    private List<RevenueByDay> revenueByDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByDay {
        private String date;
        private BigDecimal revenue;
    }
}
