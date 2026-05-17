package com.example.orgo_project.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orgo_project.dto.ReportStatsDTO;
import com.example.orgo_project.service.ReportService;

@Controller
@RequestMapping("/admin")
public class AdminReportController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/reports")
    public String showReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        // Default: Last 30 days
        LocalDateTime start = startDate != null ? 
            LocalDateTime.parse(startDate + "T00:00:00") : 
            LocalDateTime.now().minusDays(30);
            
        LocalDateTime end = endDate != null ? 
            LocalDateTime.parse(endDate + "T23:59:59") : 
            LocalDateTime.now();
        
        ReportStatsDTO stats = reportService.getReportStats(start, end);
        
        model.addAttribute("activePage", "reports");
        model.addAttribute("stats", stats);
        model.addAttribute("startDate", start.toLocalDate().toString());
        model.addAttribute("endDate", end.toLocalDate().toString());
        
        return "/pages/admin/reports";
    }
}
