package com.example.orgo_project.controller;

import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/payouts")
@Tag(name = "Payout", description = "Quản lý payout / wallet / transaction")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @GetMapping
    @Operation(summary = "Xem danh sách payout", description = "Lấy danh sách payout, lịch sử của user hiện tại và các request đang pending")
    public String index(@RequestParam(required = false, defaultValue = "ALL") String status, Model model) {
        List<WithdrawalRequest> requests = "ALL".equalsIgnoreCase(status)
                ? payoutService.findAll()
                : payoutService.findByStatus(WithdrawalStatus.valueOf(status));
        model.addAttribute("requests", requests);
        model.addAttribute("pendingRequests", payoutService.findPending());
        model.addAttribute("historyRequests", payoutService.findCurrentUserHistory());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentWallet", currentWallet());
        return "pages/admin/payouts";
    }

    @GetMapping("/seller/{id}")
    @Operation(summary = "Xem chi tiết payout seller", description = "Hiển thị chi tiết request payout của seller")
    public String sellerDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-seller";
    }

    @GetMapping("/expert/{id}")
    @Operation(summary = "Xem chi tiết payout expert", description = "Hiển thị chi tiết request payout của expert")
    public String expertDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-expert";
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo lệnh rút", description = "Seller/Expert tạo payout request mới")
    public String create(@RequestParam(required = false) Integer accountId,
                         @RequestParam BigDecimal amount,
                         @RequestParam String bankName,
                         @RequestParam String bankAccount,
                         @RequestParam String accountHolderName,
                         Model model) {
        Integer resolvedAccountId = accountId != null ? accountId : currentWallet();
        try {
            payoutService.createRequest(resolvedAccountId, amount, bankName, bankAccount, accountHolderName);
            model.addAttribute("successMessage", "Tạo lệnh rút thành công");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return index("ALL", model);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Duyệt payout", description = "Admin duyệt yêu cầu payout")
    public String approve(@PathVariable Integer id) {
        payoutService.approve(id);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Từ chối payout", description = "Admin từ chối yêu cầu payout")
    public String reject(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        payoutService.reject(id, reason == null ? "" : reason);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/paid")
    @Operation(summary = "Đánh dấu đã chi trả", description = "Admin xác nhận payout đã hoàn tất")
    public String markPaid(@PathVariable Integer id) {
        payoutService.markPaid(id);
        return "redirect:/admin/payouts";
    }

    private Integer currentWallet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails details) {
            return details.getAccount().getId();
        }
        return null;
    }
}
