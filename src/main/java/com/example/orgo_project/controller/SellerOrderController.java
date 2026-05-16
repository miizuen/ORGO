package com.example.orgo_project.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ISellerOrderService;

@Controller
@RequestMapping("/seller/orders")
public class SellerOrderController {

    private final ISellerOrderService sellerOrderService;
    private final ISellerRepository sellerRepository;

    public SellerOrderController(ISellerOrderService sellerOrderService, ISellerRepository sellerRepository) {
        this.sellerOrderService = sellerOrderService;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping
    public String mySellerOrders(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        Integer sellerId = getSellerIdFromAccount(userDetails.getAccount().getId());
        if (sellerId == null) {
            model.addAttribute("errorMessage", "Không tìm thấy thông tin seller");
            model.addAttribute("orders", java.util.Collections.emptyList());
            return "pages/seller/orders";
        }

        model.addAttribute("orders", sellerOrderService.getSellerOrders(sellerId));
        return "pages/seller/orders";
    }

    @GetMapping("/{orderId}")
    public String sellerOrderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Integer orderId,
                                    Model model) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return "redirect:/login";
        }

        Integer sellerId = getSellerIdFromAccount(userDetails.getAccount().getId());
        if (sellerId == null) {
            model.addAttribute("errorMessage", "Không tìm thấy thông tin seller");
            return "redirect:/seller/orders";
        }

        model.addAttribute("order", sellerOrderService.getSellerOrderDetail(sellerId, orderId));
        return "pages/seller/order-detail";
    }

    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId,
                               RedirectAttributes redirectAttributes) {
        Integer sellerId = getSellerIdFromAccount(userDetails.getAccount().getId());
        if (sellerId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin seller");
            return "redirect:/seller/orders";
        }

        boolean success = sellerOrderService.confirmOrder(sellerId, orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đã xác nhận đơn hàng!" : "Không thể xác nhận đơn hàng.");
        return "redirect:/seller/orders/" + orderId;
    }

    @PostMapping("/{orderId}/ship")
    public String shipOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @PathVariable Integer orderId,
                            RedirectAttributes redirectAttributes) {
        Integer sellerId = getSellerIdFromAccount(userDetails.getAccount().getId());
        if (sellerId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin seller");
            return "redirect:/seller/orders";
        }

        boolean success = sellerOrderService.shipOrder(sellerId, orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đơn hàng đã chuyển sang trạng thái đang giao!" : "Không thể chuyển trạng thái.");
        return "redirect:/seller/orders/" + orderId;
    }

    @PostMapping("/{orderId}/deliver")
    public String deliverOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable Integer orderId,
                               RedirectAttributes redirectAttributes) {
        Integer sellerId = getSellerIdFromAccount(userDetails.getAccount().getId());
        if (sellerId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin seller");
            return "redirect:/seller/orders";
        }

        boolean success = sellerOrderService.deliverOrder(sellerId, orderId);
        redirectAttributes.addFlashAttribute(success ? "successMessage" : "errorMessage",
                success ? "Đơn hàng đã giao thành công!" : "Không thể hoàn tất đơn.");
        return "redirect:/seller/orders/" + orderId;
    }

    private Integer getSellerIdFromAccount(Integer accountId) {
        // Tạo Account object với ID để query
        com.example.orgo_project.entity.Account account = new com.example.orgo_project.entity.Account();
        account.setId(accountId);
        
        return sellerRepository.findByAccount(account)
                .map(Seller::getId)
                .orElse(null);
    }
}