package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ExpertDashboardStats;
import com.example.orgo_project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/expert")
@RequiredArgsConstructor
public class ExpertController {

    private static final String ACTIVE_PAGE = "dashboard";

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String showExpertDashboard(Model model) {
        ExpertDashboardStats stats = dashboardService.getExpertDashboardStats(1L);
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/expert/dashboard";
    }
}
