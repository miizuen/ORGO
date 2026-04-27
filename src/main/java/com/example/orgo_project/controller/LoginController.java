package com.example.orgo_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.PostMapping;
@Controller
@RequestMapping("/login")
public class LoginController {
    @GetMapping("")
    public String showLogInForm() {
        return "pages/public/login";
    }

    @PostMapping("")
    public String handleLogin(@RequestParam("identifier") String identifier,
            @RequestParam("password") String password,
            Model model) {

        // Dữ liệu ảo mock login
        if ("admin".equals(password) && ("admin@orgo.com".equals(identifier) || "admin".equals(identifier))) {
            return "redirect:/admin/dashboard";
        } else if ("seller".equals(password) && ("seller@orgo.com".equals(identifier) || "seller".equals(identifier))) {
            return "redirect:/seller/dashboard";
        } else if ("expert".equals(password) && ("expert@orgo.com".equals(identifier) || "expert".equals(identifier))) {
            return "redirect:/expert/dashboard";
        } else if ("buyer".equals(password) && ("buyer@orgo.com".equals(identifier) || "buyer".equals(identifier))) {
            return "redirect:/buyer/dashboard";
        }

        model.addAttribute("error", "Đăng nhập thất bại. Tên đăng nhập hoặc mật khẩu không chính xác.");
        model.addAttribute("email", identifier);
        return "pages/public/login";
    }
}
