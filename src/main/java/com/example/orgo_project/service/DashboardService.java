package com.example.orgo_project.service;

import com.example.orgo_project.dto.AdminDashboardStats;
import com.example.orgo_project.dto.ExpertDashboardStats;
import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.repository.ArticleRepository;
import com.example.orgo_project.repository.ArticleStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ArticleRepository articleRepository;
    private final ArticleStatsRepository articleStatsRepository;
    
    public AdminDashboardStats getAdminDashboardStats() {
        AdminDashboardStats stats = new AdminDashboardStats();
        
        // Mock data
        stats.setTotalUsers(1250L);
        stats.setTotalSellers(45L);
        stats.setTotalExperts(28L);
        stats.setTotalOrders(3420L);
        stats.setTotalRevenue(new BigDecimal("125000000"));
        stats.setPendingProducts(12L);
        
        long pendingArticles = articleRepository.findByStatus(ArticleStatus.PENDING, PageRequest.of(0, 100)).getTotalElements();
        stats.setPendingArticles(pendingArticles);
        stats.setPendingPayouts(8L);
        
        // Revenue by day
        List<AdminDashboardStats.RevenueByDay> revenueByDay = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueByDay.add(new AdminDashboardStats.RevenueByDay(
                date.toString(),
                new BigDecimal(String.valueOf(1000000 + (i * 500000)))
            ));
        }
        stats.setRevenueByDay(revenueByDay);
        
        // Top products
        List<AdminDashboardStats.TopProduct> topProducts = new ArrayList<>();
        topProducts.add(new AdminDashboardStats.TopProduct(1L, "Rau hữu cơ", 150L, new BigDecimal("15000000")));
        topProducts.add(new AdminDashboardStats.TopProduct(2L, "Trái cây sạch", 120L, new BigDecimal("12000000")));
        stats.setTopProducts(topProducts);
        
        // Orders by status
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
        
        long pendingArticles = expertArticles.stream()
            .filter(a -> a.getStatus() == ArticleStatus.PENDING).count();
        stats.setPendingArticles(pendingArticles);
        
        long totalViews = expertArticles.stream()
            .mapToLong(a -> a.getViewCount() != null ? (long) a.getViewCount() : 0L)
            .sum();
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
            viewsByDay.add(new ExpertDashboardStats.ViewsByDay(
                date.toString(),
                (long) (100 + (i * 20))
            ));
        }
        stats.setViewsByDay(viewsByDay);
        
        return stats;
    }
    
    public SellerDashboardStats getSellerDashboardStats(Long sellerId) {
        SellerDashboardStats stats = new SellerDashboardStats();
        
        stats.setTotalProducts(45L);
        stats.setTotalOrders(320L);
        stats.setRevenue(new BigDecimal("25000000"));
        
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", 12L);
        ordersByStatus.put("COMPLETED", 250L);
        stats.setOrdersByStatus(ordersByStatus);
        
        List<SellerDashboardStats.RevenueByDay> revenueByDay = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueByDay.add(new SellerDashboardStats.RevenueByDay(
                date.toString(),
                new BigDecimal(String.valueOf(500000 + (i * 100000)))
            ));
        }
        stats.setRevenueByDay(revenueByDay);
        
        return stats;
    }
}
