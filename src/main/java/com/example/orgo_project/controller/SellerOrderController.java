package com.example.orgo_project.controller;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ISellerOrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private final ISellerOrderService sellerOrderService;

    public SellerOrderController(ISellerOrderService sellerOrderService) {
        this.sellerOrderService = sellerOrderService;
    }

    @GetMapping
    public List<OrderSummaryDTO> getSellerOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return sellerOrderService.getSellerOrders(userDetails.getAccount().getId());
    }

    @GetMapping("/{orderId}")
    public OrderDetailDTO getSellerOrderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Integer orderId) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        return sellerOrderService.getSellerOrderDetail(userDetails.getAccount().getId(), orderId);
    }

    @PutMapping("/{orderId}/confirm")
    public String confirmOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        sellerOrderService.confirmOrder(userDetails.getAccount().getId(), orderId);
        return "Đã xác nhận đơn hàng";
    }

    @PutMapping("/{orderId}/ship")
    public String shipOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable Integer orderId) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        sellerOrderService.shipOrder(userDetails.getAccount().getId(), orderId);
        return "Đã chuyển sang trạng thái SHIPPED";
    }

    @PutMapping("/{orderId}/deliver")
    public String deliverOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId) {
        if (userDetails == null || userDetails.getAccount() == null) {
            throw new RuntimeException("Chưa đăng nhập");
        }
        sellerOrderService.deliverOrder(userDetails.getAccount().getId(), orderId);
        return "Đã giao hàng thành công";
    }
}