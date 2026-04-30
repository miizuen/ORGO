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
public class AdminDashboardStats {
    private Long totalUsers;
    private Long totalSellers;
    private Long totalExperts;
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Long pendingProducts;
    private Long pendingArticles;
    private Long pendingPayouts;
    private List<RevenueByDay> revenueByDay;
    private List<TopProduct> topProducts;
    private Map<String, Long> ordersByStatus;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueByDay {
        private String date;
        private BigDecimal revenue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long totalSold;
        private BigDecimal revenue;
    }
}
