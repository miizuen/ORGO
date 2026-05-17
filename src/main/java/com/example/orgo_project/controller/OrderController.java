package com.example.orgo_project.controller;

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

    private Integer getUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) return null;
        if (userDetails.getAccount().getUser() != null) {
            return userDetails.getAccount().getUser().getId();
        }
        return userDetails.getAccount().getId();
    }

    @GetMapping
    public String myOrders(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @RequestParam(required = false) String status,
                           Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        Integer userId = getUserId(userDetails);

        if (status != null && !status.isBlank()) {
            try {
                com.example.orgo_project.enums.OrderStatus orderStatus =
                        com.example.orgo_project.enums.OrderStatus.valueOf(status);
                model.addAttribute("orders", ((com.example.orgo_project.service.OrderService) orderService)
                        .getMyOrdersByStatus(userId, orderStatus));
            } catch (IllegalArgumentException e) {
                model.addAttribute("orders", orderService.getMyOrders(userId));
            }
        } else {
            model.addAttribute("orders", orderService.getMyOrders(userId));
        }

        model.addAttribute("currentStatus", status);
        return "pages/user/orders";
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Integer orderId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        try {
            Integer userId = getUserId(userDetails);
            model.addAttribute("order", orderService.getOrderDetail(userId, orderId));
            return "pages/user/order-detail";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    ex.getMessage() != null && !ex.getMessage().isBlank()
                            ? ex.getMessage()
                            : "Không thể xem chi tiết đơn hàng này.");
            return "redirect:/orders";
        }
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @PathVariable Integer orderId,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        boolean success = orderService.cancelOrder(getUserId(userDetails), orderId, reason);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đơn hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng.");
        }

        return "redirect:/orders/" + orderId;
    }

    @PostMapping("/{orderId}/confirm-delivery")
    public String confirmDelivery(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @PathVariable Integer orderId,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }
        boolean success = ((com.example.orgo_project.service.OrderService) orderService)
                .confirmDelivery(getUserId(userDetails), orderId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận nhận hàng thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xác nhận nhận hàng.");
        }
        return "redirect:/orders/" + orderId;
    }

}