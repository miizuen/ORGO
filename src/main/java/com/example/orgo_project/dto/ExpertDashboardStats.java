package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertDashboardStats {
    private Long totalArticles;
    private Long totalViews;
    private Long pendingArticles;
    private Map<String, Long> articlesByStatus;
    private List<ViewsByDay> viewsByDay;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewsByDay {
        private String date;
        private Long views;
    }
}
