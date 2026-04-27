package com.example.orgo_project.controller;

import com.example.orgo_project.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "pages/public/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        Model model,
                                        HttpSession session) {
        boolean success = userService.generateAndSendOtp(email);
        if (!success) {
            model.addAttribute("errorMessage", "Email không tồn tại trong hệ thống.");
            return "pages/public/forgot-password";
        }

        session.setAttribute("resetEmail", email);
        model.addAttribute("email", email);
        return "redirect:/verify-otp";
    }

    @GetMapping("/verify-otp")
    public String verifyOtpForm(HttpSession session, Model model){
        String email = (String) session.getAttribute("resetEmail");
        if(email == null){
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        return "pages/public/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(@RequestParam("otp") String otp,
                                   HttpSession session,
                                   Model model){
        String email = (String) session.getAttribute("resetEmail");
        if(email == null){
            return "redirect:/forgot-password";
        }

        boolean isValid = userService.verifyOtp(email, otp);
        if(!isValid){
            model.addAttribute("errorMessage", "Mã OTP không hợp lệ hoặc đã hết hạn.");
            model.addAttribute("email", email);
            return "pages/public/verify-otp";
        }

        // Mark OTP as verified so it cannot be reused
        session.setAttribute("otpVerified", true);
        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session, Model model){
        String email = (String) session.getAttribute("resetEmail");
        Boolean verified = (Boolean) session.getAttribute("otpVerified");
        if(email == null || !Boolean.TRUE.equals(verified)){
            return "redirect:/forgot-password";
        }
        model.addAttribute("email", email);
        return "pages/public/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       HttpSession session,
                                       Model model){
        String email = (String) session.getAttribute("resetEmail");
        Boolean verified = (Boolean) session.getAttribute("otpVerified");
        if(email == null || !Boolean.TRUE.equals(verified)){
            return "redirect:/forgot-password";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("email", email);
            return "pages/public/reset-password";
        }
        userService.resetPassword(email, newPassword);
        session.removeAttribute("resetEmail");
        session.removeAttribute("otpVerified");
        return "redirect:/login?resetSuccess";
    }
}
