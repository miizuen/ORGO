package com.example.orgo_project.controller;

import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.PayoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/payouts")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @GetMapping
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
    public String sellerDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-seller";
    }

    @GetMapping("/expert/{id}")
    public String expertDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("request", payoutService.findById(id));
        return "pages/admin/payout-detail-expert";
    }

    @PostMapping("/create")
    public String create(@RequestParam(required = false) Integer accountId,
                         @RequestParam BigDecimal amount,
                         @RequestParam String bankName,
                         @RequestParam String bankAccount,
                         @RequestParam String accountHolderName,
                         RedirectAttributes redirectAttributes) {
        try {
            Integer resolvedAccountId = accountId != null ? accountId : currentWallet();
            payoutService.createRequest(resolvedAccountId, amount, bankName, bankAccount, accountHolderName);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo lệnh rút thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Integer id) {
        payoutService.approve(id);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        payoutService.reject(id, reason == null ? "" : reason);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/paid")
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
