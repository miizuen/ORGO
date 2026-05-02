package com.example.orgo_project.controller;

import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ICheckoutService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final ICheckoutService checkoutService;

    public CheckoutController(ICheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }
        return "pages/user/checkout";
    }

    @PostMapping
    public String placeOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @ModelAttribute CheckoutRequestDTO request,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        try {
            var response = checkoutService.checkout(userDetails.getAccount().getId(), request);
            redirectAttributes.addFlashAttribute("checkoutResult", response);
            return "redirect:/checkout/success";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/success")
    public String successPage() {
        return "pages/user/checkout-success";
    }
}