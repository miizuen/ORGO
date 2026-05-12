package com.example.orgo_project.controller;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.service.IAdminOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String ordersPage(Model model) {
        List<OrderSummaryDTO> orders = adminOrderService.getAllOrders();
        model.addAttribute("activePage", "orders");
        model.addAttribute("orders", orders);
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
