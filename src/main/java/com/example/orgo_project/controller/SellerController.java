package com.example.orgo_project.controller;

import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.DashboardService;
import com.example.orgo_project.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private static final String ACTIVE_PAGE = "dashboard";

    private final DashboardService dashboardService;
    private final PayoutService payoutService;

    @GetMapping("/dashboard")
    public String showSellerDashboard(Model model) {
        Integer accountId = currentAccountId();
        WalletBalance wallet = payoutService.getWalletByAccountId(accountId);
        BigDecimal availableBalance = wallet != null && wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
        BigDecimal heldBalance = wallet != null && wallet.getHeldBalance() != null ? wallet.getHeldBalance() : BigDecimal.ZERO;
        BigDecimal totalWithdrawn = wallet != null && wallet.getTotalWithdrawn() != null ? wallet.getTotalWithdrawn() : BigDecimal.ZERO;
        BigDecimal totalIncome = availableBalance.add(heldBalance).add(totalWithdrawn);
        SellerDashboardStats stats = dashboardService.getSellerDashboardStats(accountId != null ? accountId.longValue() : 0L);
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", ACTIVE_PAGE);
        model.addAttribute("wallet", wallet);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("settlements", java.util.List.of());
        return "pages/seller/dashboard";
    }

    @GetMapping("/wallet")
    public String showWallet(Model model) {
        Integer accountId = currentAccountId();
        WalletBalance wallet = payoutService.getWalletByAccountId(accountId);
        model.addAttribute("activePage", ACTIVE_PAGE);
        model.addAttribute("wallet", wallet);
        model.addAttribute("settlements", java.util.List.of());
        return "pages/seller/wallet";
    }

    @GetMapping("/payout")
    public String showPayoutPage(Model model) {
        Integer accountId = currentAccountId();
        WalletBalance wallet = payoutService.getWalletByAccountId(accountId);
        List<WithdrawalRequest> historyRequests = payoutService.findByAccountId(accountId);

        BigDecimal availableBalance = wallet != null && wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
        BigDecimal heldBalance = wallet != null && wallet.getHeldBalance() != null ? wallet.getHeldBalance() : BigDecimal.ZERO;
        BigDecimal totalWithdrawn = wallet != null && wallet.getTotalWithdrawn() != null ? wallet.getTotalWithdrawn() : BigDecimal.ZERO;
        BigDecimal totalIncome = availableBalance.add(heldBalance).add(totalWithdrawn);

        model.addAttribute("activePage", ACTIVE_PAGE);
        model.addAttribute("wallet", wallet);
        model.addAttribute("historyRequests", historyRequests);
        model.addAttribute("pendingAmount", heldBalance);
        model.addAttribute("totalWithdrawn", totalWithdrawn);
        model.addAttribute("totalIncome", totalIncome);
        return "pages/seller/payout";
    }

    @PostMapping("/payout/create")
    public String createPayoutRequest(
            @RequestParam BigDecimal amount,
            @RequestParam String bankName,
            @RequestParam String bankAccount,
            @RequestParam String accountHolderName,
            Model model) {
        Integer accountId = currentAccountId();
        try {
            payoutService.createRequest(accountId, amount, bankName, bankAccount, accountHolderName);
            model.addAttribute("successMessage", "Tao lenh rut tien thanh cong.");
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }
        return showPayoutPage(model);
    }

    private Integer currentAccountId() {
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
