package com.example.orgo_project.controller;

import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ICheckoutService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class CheckoutController {

    private final ICheckoutService checkoutService;

    public CheckoutController(ICheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public CheckoutResponseDTO checkout(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody CheckoutRequestDTO request) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return checkoutService.checkout(userDetails.getAccount().getId(), request);
    }
}