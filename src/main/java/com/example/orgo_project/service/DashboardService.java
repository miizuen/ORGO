package com.example.orgo_project.service;

import com.example.orgo_project.dto.AdminDashboardStats;
import com.example.orgo_project.dto.ExpertDashboardStats;
import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.repository.ArticleRepository;
import com.example.orgo_project.repository.ArticleStatsRepository;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductReviewRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ArticleRepository articleRepository;
    private final ArticleStatsRepository articleStatsRepository;
    private final ICustomerOrderRepository customerOrderRepository;
    private final ICustomerOrderItemRepository customerOrderItemRepository;
    private final IProductRepository productRepository;
    private final IProductReviewRepository productReviewRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IWalletBalanceRepository walletBalanceRepository;
    private final com.example.orgo_project.repository.IUserRepository userRepository;
    
    public AdminDashboardStats getAdminDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        
        stats.setTotalUsers(1250L);
        stats.setTotalSellers(45L);
        stats.setTotalExperts(28L);
        stats.setTotalOrders(3420L);
        stats.setTotalRevenue(new BigDecimal("125000000"));
        stats.setPendingProducts(12L);
        
        long pendingArticles = articleRepository.findByStatus(ArticleStatus.PENDING, PageRequest.of(0, 100)).getTotalElements();
        stats.setPendingArticles(pendingArticles);
        stats.setPendingPayouts(8L);
        
        List<AdminDashboardStats.RevenueByDay> revenueByDay = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueByDay.add(new AdminDashboardStats.RevenueByDay(date.toString(), new BigDecimal(String.valueOf(1000000 + (i * 500000)))));
        }
        stats.setRevenueByDay(revenueByDay);
        
        List<AdminDashboardStats.TopProduct> topProducts = new ArrayList<>();
        topProducts.add(new AdminDashboardStats.TopProduct(1L, "Rau hữu cơ", 150L, new BigDecimal("15000000")));
        topProducts.add(new AdminDashboardStats.TopProduct(2L, "Trái cây sạch", 120L, new BigDecimal("12000000")));
        stats.setTopProducts(topProducts);
        
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", 45L);
        ordersByStatus.put("COMPLETED", 3100L);
        stats.setOrdersByStatus(ordersByStatus);
        
        return stats;
    }
    
    public ExpertDashboardStats getExpertDashboardStats(Long expertId) {
        ExpertDashboardStats stats = new ExpertDashboardStats();
        
        List<com.example.orgo_project.entity.Article> expertArticles = articleRepository.findByExpertId(expertId.intValue(), PageRequest.of(0, 1000)).getContent();
        long totalArticles = expertArticles.size();
        stats.setTotalArticles(totalArticles);
        
        long pendingArticles = expertArticles.stream().filter(a -> a.getStatus() == ArticleStatus.PENDING).count();
        stats.setPendingArticles(pendingArticles);
        
        long totalViews = expertArticles.stream().mapToLong(a -> a.getViewCount() != null ? (long) a.getViewCount() : 0L).sum();
        stats.setTotalViews(totalViews);
        
        Map<String, Long> articlesByStatus = new HashMap<>();
        articlesByStatus.put("DRAFT", expertArticles.stream().filter(a -> a.getStatus() == ArticleStatus.DRAFT).count());
        articlesByStatus.put("PENDING", pendingArticles);
        articlesByStatus.put("PUBLISHED", expertArticles.stream().filter(a -> a.getStatus() == ArticleStatus.PUBLISHED).count());
        articlesByStatus.put("REJECTED", expertArticles.stream().filter(a -> a.getStatus() == ArticleStatus.REJECTED).count());
        stats.setArticlesByStatus(articlesByStatus);
        
        List<ExpertDashboardStats.ViewsByDay> viewsByDay = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            viewsByDay.add(new ExpertDashboardStats.ViewsByDay(date.toString(), (long) (100 + (i * 20))));
        }
        stats.setViewsByDay(viewsByDay);
        
        return stats;
    }
    
    public SellerDashboardStats getSellerDashboardStats(Long sellerAccountId) {
        SellerDashboardStats stats = new SellerDashboardStats();
        
        List<CustomerOrder> orders = customerOrderRepository.findBySellerIdOrderByOrderedAtDesc(sellerAccountId.intValue());
        long totalOrders = orders.size();
        BigDecimal revenue = orders.stream().map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        stats.setTotalProducts(orders.isEmpty() ? 0L : (long) orders.size());
        stats.setTotalOrders(totalOrders);
        stats.setRevenue(revenue);
        
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PENDING).count());
        ordersByStatus.put("PROCESSING", orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.PROCESSING).count());
        ordersByStatus.put("DELIVERED", orders.stream().filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED).count());
        stats.setOrdersByStatus(ordersByStatus);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        List<SellerDashboardStats.RecentOrder> recentOrders = orders.stream()
                .limit(5)
                .map(order -> {
                    String customerName = "Khách hàng " + order.getUserId();
                    com.example.orgo_project.entity.UserProfile profile = userRepository.findById(order.getUserId()).orElse(null);
                    if (profile != null) {
                        customerName = profile.getFullName();
                    }

                    List<CustomerOrderItem> orderItems = customerOrderItemRepository.findByOrderIdIn(List.of(order.getId()));
                    String productsSummary = "";
                    if (!orderItems.isEmpty()) {
                        CustomerOrderItem firstItem = orderItems.get(0);
                        ProductVariant variant = productVariantRepository.findById(firstItem.getProductVariantId()).orElse(null);
                        Product product = variant != null ? productRepository.findById(variant.getProductId()).orElse(null) : null;
                        
                        String firstProdName = product != null ? product.getProductName() : "Sản phẩm";
                        String firstVarName = variant != null ? variant.getVariantName() : "";
                        if (firstVarName != null && !firstVarName.isBlank()) {
                            productsSummary = firstProdName + " (" + firstVarName + ")";
                        } else {
                            productsSummary = firstProdName;
                        }

                        if (orderItems.size() > 1) {
                            int extraItems = orderItems.size() - 1;
                            productsSummary += " +" + extraItems + " item" + (extraItems > 1 ? "s" : "");
                        }
                    } else {
                        productsSummary = "Không có sản phẩm";
                    }

                    return new SellerDashboardStats.RecentOrder(
                            order.getId(),
                            order.getOrderCode(),
                            order.getOrderStatus() != null ? order.getOrderStatus().name() : null,
                            order.getTotalAmount(),
                            order.getOrderedAt() != null ? order.getOrderedAt().format(formatter) : "",
                            customerName,
                            productsSummary
                    );
                })
                .toList();
        stats.setRecentOrders(recentOrders);

        List<Integer> orderIds = orders.stream().map(CustomerOrder::getId).toList();
        List<CustomerOrderItem> items = orderIds.isEmpty() ? List.of() : customerOrderItemRepository.findByOrderIdIn(orderIds);
        Map<Integer, SellerDashboardStats.TopProduct> topMap = new HashMap<>();
        for (CustomerOrderItem item : items) {
            Integer productId = resolveProductId(item);
            if (productId == null) continue;
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) continue;
            long quantity = item.getQuantity() != null ? item.getQuantity().longValue() : 0L;
            BigDecimal lineTotal = item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO;
            SellerDashboardStats.TopProduct current = topMap.get(productId);
            if (current == null) {
                Double averageRating = productReviewRepository.findAverageRatingByProductId(productId);
                long reviewCount = productReviewRepository.countByProductId(productId);
                topMap.put(productId, new SellerDashboardStats.TopProduct(
                        productId,
                        product.getProductName(),
                        product.getImageUrl(),
                        quantity,
                        averageRating,
                        reviewCount,
                        lineTotal
                ));
            } else {
                current.setSoldQuantity(current.getSoldQuantity() + quantity);
                current.setRevenue(current.getRevenue().add(lineTotal));
            }
        }
        stats.setTopProducts(topMap.values().stream()
                .sorted((a, b) -> b.getSoldQuantity().compareTo(a.getSoldQuantity()))
                .limit(5)
                .toList());
        
        List<SellerDashboardStats.RevenueByDay> revenueByDay = new ArrayList<>();
        Map<LocalDate, BigDecimal> dailyRevenueMap = new HashMap<>();
        for (CustomerOrder order : orders) {
            if (order.getOrderedAt() != null && order.getOrderStatus() != OrderStatus.CANCELLED) {
                LocalDate date = order.getOrderedAt().toLocalDate();
                BigDecimal orderAmt = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                dailyRevenueMap.put(date, dailyRevenueMap.getOrDefault(date, BigDecimal.ZERO).add(orderAmt));
            }
        }
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            BigDecimal dayRevenue = dailyRevenueMap.getOrDefault(date, BigDecimal.ZERO);
            revenueByDay.add(new SellerDashboardStats.RevenueByDay(date.toString(), dayRevenue));
        }
        stats.setRevenueByDay(revenueByDay);
        
        return stats;
    }

    private Integer resolveProductId(CustomerOrderItem item) {
        if (item.getProductVariantId() == null) return null;
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
        return variant != null ? variant.getProductId() : null;
    }
}
