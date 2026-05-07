package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.IOrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String myOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        Integer accountId = userDetails.getAccount().getId();
        model.addAttribute("orders", orderService.getMyOrders(accountId));
        return "pages/user/orders";
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Integer orderId,
                              Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        Integer accountId = userDetails.getAccount().getId();
        model.addAttribute("order", orderService.getOrderDetail(accountId, orderId));
        return "pages/user/order-detail";
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Integer orderId,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        boolean success = orderService.cancelOrder(userDetails.getAccount().getId(), orderId, reason);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng.");
        }

        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/return")
    public String requestReturn(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @PathVariable Integer orderId,
                                @RequestParam(required = false) String reason,
                                @RequestParam(required = false) String requestType,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        ReturnRequestDTO request = new ReturnRequestDTO();
        request.setOrderId(orderId);
        request.setReason(reason);

        boolean success = orderService.requestReturn(userDetails.getAccount().getId(), request);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã gửi yêu cầu hoàn trả!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể gửi yêu cầu hoàn trả.");
        }

        return "redirect:/orders/" + orderId;
    }
}