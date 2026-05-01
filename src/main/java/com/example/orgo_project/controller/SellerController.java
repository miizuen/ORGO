package com.example.orgo_project.controller;

import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private static final String ACTIVE_PAGE = "dashboard";

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showSellerDashboard(Model model) {
        SellerDashboardStats stats = dashboardService.getSellerDashboardStats(1L);
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/seller/dashboard";
    }

    @GetMapping("/payout")
    public String showPayoutPage(Model model){
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/seller/payout";
    }
}
