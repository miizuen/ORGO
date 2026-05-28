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
public class ExpertDashboardStats {
    private Long totalArticles;
    private Long totalViews;
    private Long pendingArticles;
    private Long totalOrders;
    private BigDecimal totalCommission;
    private Map<String, Long> articlesByStatus;
    private List<ViewsByDay> viewsByDay;
    private List<ArticlesByWeek> articlesByWeek;
    private List<ArticlesByMonth> articlesByMonth;
    private List<TopArticle> topArticles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewsByDay {
        private String date;
        private Long views;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlesByWeek {
        private String week;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlesByMonth {
        private String month;
        private Long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopArticle {
        private Integer id;
        private String title;
        private Long views;
        private String coverImage;
        private String publishedAt;
    }
}
