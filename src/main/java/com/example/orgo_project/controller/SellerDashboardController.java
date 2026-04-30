package com.example.orgo_project.controller;

import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/dashboard")
@RequiredArgsConstructor
public class SellerDashboardController {
    
    private final DashboardService dashboardService;
    
    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerDashboardStats> getSellerDashboardStats(@PathVariable Long sellerId) {
        return ResponseEntity.ok(dashboardService.getSellerDashboardStats(sellerId));
    }
}
