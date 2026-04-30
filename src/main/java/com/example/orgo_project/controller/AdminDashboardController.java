package com.example.orgo_project.controller;

import com.example.orgo_project.dto.AdminDashboardStats;
import com.example.orgo_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping
    public ResponseEntity<AdminDashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardStats());
    }
}
