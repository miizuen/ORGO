package com.example.orgo_project.controller;

import com.example.orgo_project.dto.SellerDashboardStats;
import com.example.orgo_project.entity.WalletBalance;
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

@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private static final String ACTIVE_PAGE = "dashboard";

    private final DashboardService dashboardService;
    private final PayoutService payoutService;

    @GetMapping("/dashboard")
    public String showSellerDashboard(Model model) {
        SellerDashboardStats stats = dashboardService.getSellerDashboardStats(1L);
        model.addAttribute("stats", stats);
        model.addAttribute("activePage", ACTIVE_PAGE);

        // Check maintenance balance
        WalletBalance wallet = payoutService.getWalletByAccountId(currentAccountId());
        if (wallet != null && wallet.getMaintenanceBalance() != null) {
            BigDecimal available = wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO;
            BigDecimal maintenance = wallet.getMaintenanceBalance();
            if (available.compareTo(maintenance) < 0) {
                model.addAttribute("maintenanceWarning", "Số dư của bạn thấp hơn mức duy trì tối thiểu (" + maintenance + " VND). Vui lòng nạp thêm để tiếp tục hoạt động.");
            } else if (available.subtract(maintenance).compareTo(new BigDecimal("5000")) < 0) {
                model.addAttribute("maintenanceWarning", "Số dư của bạn sắp đạt mức duy trì tối thiểu. Cân nhắc nạp thêm.");
            }
        }

        return "pages/seller/dashboard";
    }

    @GetMapping("/payout")
    public String showPayoutPage(Model model){
        model.addAttribute("activePage", ACTIVE_PAGE);
        model.addAttribute("disabledMessage", "Chức năng rút tiền thủ công đã bị vô hiệu hóa. Tiền sẽ được tự động chuyển vào ví sau khi đơn hàng hoàn thành.");
        model.addAttribute("sellerBankInfoNotice", "Khi đăng ký Seller, vui lòng cập nhật sẵn thông tin ngân hàng để hệ thống có thể đối soát và chi trả tự động.");
        return "pages/seller/payout";
    }

    @PostMapping("/payout/create")
    public String createPayoutRequest(
            @RequestParam BigDecimal amount,
            @RequestParam String bankName,
            @RequestParam String bankAccount,
            @RequestParam String accountHolderName,
            Model model) {
        model.addAttribute("errorMessage", "Chức năng rút tiền thủ công đã bị vô hiệu hóa.");
        model.addAttribute("bankInfoReminder", "Hệ thống hiện không tạo lệnh rút. Thông tin ngân hàng của Seller sẽ được dùng cho đối soát và thanh toán tự động trong các đơn hàng escrow.");
        model.addAttribute("activePage", ACTIVE_PAGE);
        return "pages/seller/payout";
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
