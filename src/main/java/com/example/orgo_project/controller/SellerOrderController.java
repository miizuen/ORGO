package com.example.orgo_project.controller;

import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ISellerOrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final ISellerOrderService sellerOrderService;

    public SellerOrderController(ISellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public String mySellerOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        model.addAttribute("orders", sellerOrderService.getSellerOrders(userDetails.getAccount().getId()));
        return "pages/seller/orders";
    }

    @GetMapping("/{orderId}")
    public String sellerOrderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Integer orderId,
                                    Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        model.addAttribute("order", sellerOrderService.getSellerOrderDetail(userDetails.getAccount().getId(), orderId));
        return "pages/seller/order-detail";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId,
                               RedirectAttributes redirectAttributes) {
        boolean success = sellerOrderService.confirmOrder(userDetails.getAccount().getId(), orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đã xác nhận đơn hàng!" : "Không thể xác nhận đơn hàng.");
        return "redirect:/seller/orders/" + orderId;
    }

    @PostMapping("/{orderId}/ship")
    public String shipOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable Integer orderId,
                            RedirectAttributes redirectAttributes) {
        boolean success = sellerOrderService.shipOrder(userDetails.getAccount().getId(), orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đơn hàng đã chuyển sang trạng thái đang giao!" : "Không thể chuyển trạng thái.");
        return "redirect:/seller/orders/" + orderId;
    }

    @PostMapping("/{orderId}/deliver")
    public String deliverOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId,
                               RedirectAttributes redirectAttributes) {
        boolean success = sellerOrderService.deliverOrder(userDetails.getAccount().getId(), orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đơn hàng đã giao thành công!" : "Không thể hoàn tất đơn.");
        return "redirect:/seller/orders/" + orderId;
    }
}