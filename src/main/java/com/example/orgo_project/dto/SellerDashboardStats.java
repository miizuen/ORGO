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
    private List<RecentOrder> recentOrders;
    private List<TopProduct> topProducts;

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
    public static class RecentOrder {
        private Integer id;
        private String orderCode;
        private String orderStatus;
        private BigDecimal totalAmount;
        private String orderedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopProduct {
        private Integer productId;
        private String productName;
        private Long soldQuantity;
        private BigDecimal revenue;
    }
}
