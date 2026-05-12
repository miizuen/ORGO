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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/payouts")
@Tag(name = "Payout", description = "Quan ly payout / wallet / transaction")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @GetMapping
    @Operation(summary = "Xem danh sach payout", description = "Lay danh sach payout, lich su cua user hien tai va cac request dang pending")
    public String index(@RequestParam(required = false, defaultValue = "ALL") String status, Model model) {
        List<WithdrawalRequest> requests = "ALL".equalsIgnoreCase(status)
                ? payoutService.findAll()
                : payoutService.findByStatus(WithdrawalStatus.valueOf(status));
        model.addAttribute("allPayouts", requests);
        model.addAttribute("pendingPayouts", payoutService.findPending());
        model.addAttribute("historyRequests", payoutService.findCurrentUserHistory());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentWallet", currentWallet());
        return "pages/admin/payouts";
    }

    @GetMapping("/seller/{id}")
    @Operation(summary = "Xem chi tiet payout seller", description = "Hien thi chi tiet request payout cua seller")
    public String sellerDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-seller";
    }

    @GetMapping("/expert/{id}")
    @Operation(summary = "Xem chi tiet payout expert", description = "Hien thi chi tiet request payout cua expert")
    public String expertDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-expert";
    }

    @PostMapping("/create")
    @Operation(summary = "Tao lenh rut", description = "Seller/Expert tao payout request moi")
    public String create(@RequestParam(required = false) Integer accountId,
                         @RequestParam BigDecimal amount,
                         @RequestParam String bankName,
                         @RequestParam String bankAccount,
                         @RequestParam String accountHolderName,
                         Model model) {
        Integer resolvedAccountId = accountId != null ? accountId : currentWallet();
        try {
            payoutService.createRequest(resolvedAccountId, amount, bankName, bankAccount, accountHolderName);
            model.addAttribute("successMessage", "Tao lenh rut thanh cong");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return index("ALL", model);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Duyet payout", description = "Admin duyet yeu cau payout")
    public String approve(@PathVariable Integer id,
                          @RequestParam(required = false) String transactionCode,
                          RedirectAttributes redirectAttributes) {
        try {
            payoutService.approve(id, transactionCode);
            redirectAttributes.addFlashAttribute("successMessage", "Duyet payout thanh cong.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/payouts";
    }

    @GetMapping("/{id}/approve")
    public String approveGetFallback(@PathVariable Integer id,
                                     @RequestParam(required = false) String transactionCode,
                                     RedirectAttributes redirectAttributes) {
        try {
            payoutService.approve(id, transactionCode);
            redirectAttributes.addFlashAttribute("successMessage", "Duyet payout thanh cong.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Tu choi payout", description = "Admin tu choi yeu cau payout")
    public String reject(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        payoutService.reject(id, reason == null ? "" : reason);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/paid")
    @Operation(summary = "Danh dau da chi tra", description = "Admin xac nhan payout da hoan tat")
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
