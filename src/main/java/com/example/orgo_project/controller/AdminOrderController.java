package com.example.orgo_project.controller;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.service.IAdminOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final IAdminOrderService adminOrderService;

    public AdminOrderController(IAdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public String ordersPage(@RequestParam(required = false) String orderStatus,
                             @RequestParam(required = false) String paymentStatus,
                             Model model) {
        List<OrderSummaryDTO> orders = adminOrderService.getAllOrders();

        if (orderStatus != null && !orderStatus.isBlank() && !"ALL".equalsIgnoreCase(orderStatus)) {
            orders = orders.stream()
                    .filter(o -> o.getOrderStatus() != null && o.getOrderStatus().equalsIgnoreCase(orderStatus))
                    .toList();
        }

        if (paymentStatus != null && !paymentStatus.isBlank() && !"ALL".equalsIgnoreCase(paymentStatus)) {
            orders = orders.stream()
                    .filter(o -> o.getPaymentStatus() != null && o.getPaymentStatus().equalsIgnoreCase(paymentStatus))
                    .toList();
        }

        model.addAttribute("activePage", "orders");
        model.addAttribute("orders", orders);
        model.addAttribute("orderStatus", orderStatus != null ? orderStatus : "ALL");
        model.addAttribute("paymentStatus", paymentStatus != null ? paymentStatus : "ALL");
        return "pages/admin/orders";
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Integer orderId, Model model) {
        OrderDetailDTO order = adminOrderService.getOrderDetail(orderId);
        model.addAttribute("activePage", "orders");
        model.addAttribute("order", order);
        return "pages/admin/order-detail";
    }

    @PostMapping("/{orderId}/approve")
    public String approveOrder(@PathVariable Integer orderId, RedirectAttributes redirectAttributes) {
        adminOrderService.approveOrder(orderId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã duyệt đơn hàng và kích hoạt chia tiền.");
        return "redirect:/admin/orders/" + orderId;
    }
}