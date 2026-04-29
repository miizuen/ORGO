package com.example.orgo_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.security.Principal;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping()
    public String showHome(Model model, Principal principal) {
        String mess = "";
        if (principal != null) {
            mess = "Bạn đã đăng nhập vào hệ thống! Hãy trải nghiệm.";
        } else {
            mess = "Xin chào các bạn đến với trang web của chúng tôi! Hãy đăng nhập để trải nghiệm tốt hơn.";
        }
        model.addAttribute("message", mess);
        return "pages/public/home";
    }

    @GetMapping("seller/dashboard")
    public String showSellerDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "pages/seller/dashboard";
    }

    @GetMapping("expert/dashboard")
    public String showExpertDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "pages/expert/dashboard";
    }

    @GetMapping("buyer/dashboard")
    public String showBuyerDashboard(Model model) {
        model.addAttribute("activePage", "dashboard");
        return "pages/buyer/dashboard";
    }
    @GetMapping("logout")
    public String logOut(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
