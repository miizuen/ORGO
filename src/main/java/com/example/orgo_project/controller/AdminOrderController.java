package com.example.orgo_project.controller;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.service.IAdminOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final IAdminOrderService adminOrderService;

    public AdminOrderController(IAdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public List<OrderSummaryDTO> getAllOrders() {
        return adminOrderService.getAllOrders();
    }

    @GetMapping("/{orderId}")
    public OrderDetailDTO getOrderDetail(@PathVariable Integer orderId) {
        return adminOrderService.getOrderDetail(orderId);
    }

    @GetMapping("/returns")
    public List<ReturnRequestDTO> getReturnRequests() {
        return adminOrderService.getReturnRequests();
    }

    @PutMapping("/returns/{returnId}/approve")
    public String approveReturn(@PathVariable Integer returnId) {
        adminOrderService.approveReturn(returnId);
        return "Đã duyệt yêu cầu hoàn trả";
    }

    @PutMapping("/returns/{returnId}/reject")
    public String rejectReturn(@PathVariable Integer returnId) {
        adminOrderService.rejectReturn(returnId);
        return "Đã từ chối yêu cầu hoàn trả";
    }
}