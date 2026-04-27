package com.example.orgo_project.controller;

import com.example.orgo_project.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.PostMapping;
@Controller
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private IAccountService accountService;

    @GetMapping("")
    public String showLogInForm() {
        return "pages/public/login";
    }

//    @PostMapping("")
//    public String handleLogin(@RequestParam("username") String username,
//            @RequestParam("password") String password,
//            Model model) {
//
//        accountService.findByUsername(username);
//
//        model.addAttribute("error", "Đăng nhập thất bại. Tên đăng nhập hoặc mật khẩu không chính xác.");
//        model.addAttribute("username", username);
//        return "pages/public/login";
//    }
}
