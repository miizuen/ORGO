package com.example.orgo_project.controller;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.RoleName;
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
import java.util.Set;

@Controller
@RequestMapping("/admin/payouts")
@Tag(name = "Payout", description = "Quan ly payout / wallet / transaction")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;
    @Autowired
    private com.example.orgo_project.repository.IAccountRepository accountRepository;
    @Autowired
    private com.example.orgo_project.repository.IWalletBalanceRepository walletBalanceRepository;
    @Autowired
    private com.example.orgo_project.repository.ITransactionHistoryRepository transactionHistoryRepository;
    @Autowired
    private com.example.orgo_project.repository.ICustomerOrderRepository customerOrderRepository;

    @GetMapping
    @Operation(summary = "Xem danh sach payout", description = "Lay danh sach payout va tach theo seller/expert")
    public String index(@RequestParam(required = false, defaultValue = "ALL") String status, Model model) {
        List<WithdrawalRequest> requests = "ALL".equalsIgnoreCase(status)
                ? payoutService.findAll()
                : payoutService.findByStatus(WithdrawalStatus.valueOf(status));

        List<WithdrawalRequest> sellerPayouts = requests.stream().filter(this::isSellerRequest).toList();
        List<WithdrawalRequest> expertPayouts = requests.stream().filter(this::isExpertRequest).toList();
        List<WithdrawalRequest> pendingSellerPayouts = sellerPayouts.stream().filter(p -> p.getStatus() == WithdrawalStatus.PENDING).toList();
        List<WithdrawalRequest> pendingExpertPayouts = expertPayouts.stream().filter(p -> p.getStatus() == WithdrawalStatus.PENDING).toList();

        model.addAttribute("sellerPayouts", sellerPayouts);
        model.addAttribute("expertPayouts", expertPayouts);
        model.addAttribute("pendingSellerPayouts", pendingSellerPayouts);
        model.addAttribute("pendingExpertPayouts", pendingExpertPayouts);
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
    @Operation(summary = "Xem chi tiet payout expert", description = "Hien thi chi tiet request payout cua expert + bang chung commission")
    public String expertDetail(@PathVariable Integer id, Model model) {
        WithdrawalRequest request = payoutService.findById(id);
        model.addAttribute("request", request);

        WalletBalance wallet = walletBalanceRepository.findByAccountId(request.getRequesterId()).orElse(null);
        List<TransactionHistory> commissionRows = wallet != null
                ? transactionHistoryRepository.findByWalletIdAndTypeOrderByCreatedAtDesc(wallet.getId(), "EXPERT_COMMISSION")
                : List.of();
        Set<Integer> orderIds = commissionRows.stream()
                .map(TransactionHistory::getReferenceId)
                .filter(ref -> ref != null && ref > 0)
                .collect(java.util.stream.Collectors.toSet());
        List<CustomerOrder> proofOrders = customerOrderRepository.findAllById(orderIds).stream()
                .filter(o -> o.getArticleId() != null)
                .toList();

        model.addAttribute("commissionRows", commissionRows);
        model.addAttribute("proofOrders", proofOrders);
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

    private boolean isSellerRequest(WithdrawalRequest request) {
        Account account = accountRepository.findById(request.getRequesterId()).orElse(null);
        return account != null && account.getRole() != null && account.getRole().getRoleName() == RoleName.SELLER;
    }

    private boolean isExpertRequest(WithdrawalRequest request) {
        Account account = accountRepository.findById(request.getRequesterId()).orElse(null);
        return account != null && account.getRole() != null && account.getRole().getRoleName() == RoleName.EXPERT;
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
