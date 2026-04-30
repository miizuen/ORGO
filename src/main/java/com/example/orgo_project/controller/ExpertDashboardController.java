package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ExpertDashboardStats;
import com.example.orgo_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expert/dashboard")
@RequiredArgsConstructor
public class ExpertDashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/{expertId}")
    public ResponseEntity<ExpertDashboardStats> getExpertDashboardStats(@PathVariable Long expertId) {
        return ResponseEntity.ok(dashboardService.getExpertDashboardStats(expertId));
    }
}
