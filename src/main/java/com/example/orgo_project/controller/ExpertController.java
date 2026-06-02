package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ExpertDashboardStats;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ArticleService;
import com.example.orgo_project.service.DashboardService;
import com.example.orgo_project.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/expert")
@RequiredArgsConstructor
public class ExpertController {

    private static final String ACTIVE_PAGE = "dashboard";

    private final DashboardService dashboardService;
    private final ArticleService articleService;
    private final PayoutService payoutService;

    @GetMapping("/dashboard")
    public String showExpertDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String expertName = "Chuyen gia";
        Integer accountId = 1;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            accountId = userDetails.getAccount().getId();
            expertName = userDetails.getAccount().getUsername();
            if (userDetails.getAccount().getUser() != null && userDetails.getAccount().getUser().getFullName() != null) {
                expertName = userDetails.getAccount().getUser().getFullName();
            }
        }

        ExpertDashboardStats stats = dashboardService.getExpertDashboardStats(accountId.longValue());
        Page<com.example.orgo_project.dto.ArticleResponse> articles = articleService.getExpertArticles(accountId, PageRequest.of(0, 10));
        WalletBalance wallet = payoutService.getWalletByAccountId(accountId);
        BigDecimal availableBalance = wallet != null && wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
        BigDecimal heldBalance = wallet != null && wallet.getHeldBalance() != null ? wallet.getHeldBalance() : BigDecimal.ZERO;
        BigDecimal totalWithdrawn = wallet != null && wallet.getTotalWithdrawn() != null ? wallet.getTotalWithdrawn() : BigDecimal.ZERO;
        BigDecimal totalIncome = availableBalance.add(heldBalance).add(totalWithdrawn);

        // Tổng hoa hồng = tổng tiền đã nhận vào wallet (available + held + withdrawn)
        stats.setTotalCommission(totalIncome);

        // Đếm đơn hàng từ lịch sử giao dịch commission
        long commissionOrderCount = payoutService.findTransactionHistoryByAccountId(accountId).stream()
                .filter(item -> "EXPERT_COMMISSION".equals(item.getType()))
                .count();
        stats.setTotalOrders(commissionOrderCount);

        model.addAttribute("stats", stats);
        model.addAttribute("articles", articles);
        model.addAttribute("totalIncome", totalIncome);
        model.addAttribute("expertName", expertName);
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/expert/dashboard";
    }

    @GetMapping("/payout")
    public String showPayoutPage(Model model) {
        Integer accountId = currentAccountId();
        WalletBalance wallet = payoutService.getWalletByAccountId(accountId);
        BigDecimal availableBalance = wallet != null && wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
        BigDecimal heldBalance = wallet != null && wallet.getHeldBalance() != null ? wallet.getHeldBalance() : BigDecimal.ZERO;
        BigDecimal totalWithdrawn = wallet != null && wallet.getTotalWithdrawn() != null ? wallet.getTotalWithdrawn() : BigDecimal.ZERO;
        BigDecimal totalIncome = availableBalance.add(heldBalance).add(totalWithdrawn);

        List<TransactionHistory> commissions = payoutService.findTransactionHistoryByAccountId(accountId).stream()
                .filter(item -> "EXPERT_COMMISSION".equals(item.getType()))
                .toList();

        model.addAttribute("activePage", ACTIVE_PAGE);
        model.addAttribute("wallet", wallet);
        model.addAttribute("commissions", commissions);
        model.addAttribute("historyRequests", payoutService.findByAccountId(accountId));
        model.addAttribute("pendingAmount", heldBalance);
        model.addAttribute("totalWithdrawn", totalWithdrawn);
        model.addAttribute("totalIncome", totalIncome);
        return "pages/expert/payout";
    }

    @PostMapping("/payout/create")
    public String createPayoutRequest(
            @RequestParam BigDecimal amount,
            @RequestParam String bankName,
            @RequestParam String bankAccount,
            @RequestParam String accountHolderName,
            Model model) {
        try {
            payoutService.createRequest(currentAccountId(), amount, bankName, bankAccount, accountHolderName);
            model.addAttribute("successMessage", "Yeu cau rut tien da duoc gui");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
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
