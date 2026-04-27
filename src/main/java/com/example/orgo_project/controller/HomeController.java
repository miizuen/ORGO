package com.example.orgo_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping()
    public String showHome() {
        return "pages/public/home";
    }

    @GetMapping("admin/dashboard")
    public String showAdminDashboard() {
        return "pages/admin/dashboard";
    }

    @GetMapping("seller/dashboard")
    public String showSellerDashboard() {
        return "pages/seller/dashboard";
    }

    @GetMapping("expert/dashboard")
    public String showExpertDashboard() {
        return "pages/expert/dashboard";
    }

    @GetMapping("buyer/dashboard")
    public String showBuyerDashboard() {
        return "pages/buyer/dashboard";
    }
    @GetMapping("logout")
    public String logOut(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    

}
