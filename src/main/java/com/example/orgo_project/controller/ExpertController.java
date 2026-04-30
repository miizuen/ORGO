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
    private final com.example.orgo_project.service.ArticleService articleService;

    @GetMapping("/dashboard")
    public String showExpertDashboard(Model model) {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String expertName = "Chuyên gia";
        Integer accountId = 1;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.example.orgo_project.security.CustomUserDetails userDetails) {
            accountId = userDetails.getAccount().getId();
            expertName = userDetails.getAccount().getUsername();
            if (userDetails.getAccount().getUser() != null && userDetails.getAccount().getUser().getFullName() != null) {
                expertName = userDetails.getAccount().getUser().getFullName();
            }
        }
        
        ExpertDashboardStats stats = dashboardService.getExpertDashboardStats(accountId.longValue());
        org.springframework.data.domain.Page<com.example.orgo_project.dto.ArticleResponse> articles = articleService.getExpertArticles(accountId, org.springframework.data.domain.PageRequest.of(0, 10));
        
        model.addAttribute("stats", stats);
        model.addAttribute("articles", articles);
        model.addAttribute("expertName", expertName);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/expert/dashboard";
    }
}
