package com.example.orgo_project.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.orgo_project.config.PaymentQrProperties;
import com.example.orgo_project.dto.ExpertDTO;
import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.PaymentBankConfig;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.repository.ArticleRepository;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IExpertRepository;
import com.example.orgo_project.repository.IOrderSettlementRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import com.example.orgo_project.repository.ProductRepository;
import com.example.orgo_project.service.IAdminOrderService;
import com.example.orgo_project.service.IExpertService;
import com.example.orgo_project.service.ISellerService;
import com.example.orgo_project.service.PaymentBankConfigService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ISellerService sellerService;
    @Autowired private IExpertService expertService;
    @Autowired private IAdminOrderService adminOrderService;
    @Autowired private PaymentQrProperties paymentQrProperties;
    @Autowired private ISellerRepository sellerRepository;
    @Autowired private IExpertRepository expertRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private IWalletBalanceRepository walletBalanceRepository;
    @Autowired private IOrderSettlementRepository orderSettlementRepository;
    @Autowired private IAccountRepository accountRepository;
    @Autowired private PaymentBankConfigService paymentBankConfigService;

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        var pendingOrders = adminOrderService.getAllOrders().stream()
                .filter(order -> "PAID".equals(order.getPaymentStatus()) && "PENDING".equals(order.getOrderStatus()))
                .limit(5)
                .toList();
        List<OrderSettlement> allSettlements = orderSettlementRepository.findAll();
        BigDecimal totalAdminCommission = allSettlements.stream()
                .map(item -> item.getCommissionAmount() != null ? item.getCommissionAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("sellerCount", sellerRepository.count());
        model.addAttribute("expertCount", expertRepository.count());
        model.addAttribute("pendingSellerCount", sellerRepository.findByStatus(com.example.orgo_project.enums.SellerStatus.PENDING).size());
        model.addAttribute("pendingExpertCount", expertRepository.findByStatus(com.example.orgo_project.enums.ExpertStatus.PENDING).size());
        model.addAttribute("pendingArticleCount", articleRepository.findByStatus(com.example.orgo_project.enums.ArticleStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("pendingOrderCount", pendingOrders.size());
        model.addAttribute("orders", pendingOrders);
        model.addAttribute("adminBankName", paymentQrProperties.getBankName());
        model.addAttribute("adminBankAccount", paymentQrProperties.getAccountNumber());
        model.addAttribute("adminAccountHolderName", paymentQrProperties.getAccountHolderName());
        model.addAttribute("adminWallet", walletBalanceRepository.findByAccountId(resolveAdminAccountId()).orElse(null));
        model.addAttribute("adminSettlements", allSettlements.stream().filter(item -> item.getSellerId() == null || item.getSellerId() <= 0).toList());
        model.addAttribute("totalAdminCommission", totalAdminCommission);
        model.addAttribute("bankConfig", paymentBankConfigService.getActiveConfig());
        return "/pages/admin/dashboard";
    }

    @GetMapping("/seller-pending-list")
    public String showSellerPendingList(Model model){
        model.addAttribute("activePage", "seller-pending-list");
        model.addAttribute("sellers", sellerService.getPendingList());
        return "/pages/admin/seller-pending-list";
    }

    @GetMapping("/seller-pending-list/seller-approve-detail")
    public String showSellerPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("activePage", "seller-pending-list");
        model.addAttribute("seller", sellerService.findById(id));
        return "/pages/admin/seller-approve-detail";
    }

    @PostMapping("/seller-pending-list/approve")
    public String approveSeller(@RequestParam int id, Model model){
        sellerService.approve(id);
        model.addAttribute("successMessage", "Đã phê duyệt thành công!");
        return "redirect:/admin/seller-pending-list?approved";
    }

    @PostMapping("/seller-pending-list/reject")
    public String rejectSeller(@RequestParam int id, Model model){
        sellerService.reject(id);
        model.addAttribute("successMessage", "Đã từ chối thành công!");
        return "redirect:/admin/seller-pending-list?rejected";
    }

    @GetMapping("/expert-pending-list")
    public String showExpertPendingList(Model model){
        model.addAttribute("activePage", "expert-pending-list");
        model.addAttribute("experts", expertService.getPendingList().stream().map(ExpertDTO::fromEntity).collect(Collectors.toList()));
        return "/pages/admin/expert-pending-list";
    }

    @GetMapping("/expert-pending-list/expert-approve-detail")
    public String showExpertPendingDetail(@RequestParam int id, Model model){
        model.addAttribute("activePage", "expert-pending-list");
        model.addAttribute("expert", ExpertDTO.fromEntity(expertService.findById(id)));
        return "/pages/admin/expert-approve-detail";
    }
    @PostMapping("/expert-pending-list/approve")
    public String approveExpert(@RequestParam int id, Model model){
        expertService.approve(id);
        model.addAttribute("successMessage", "Đã phê duyệt thành công!");
        return "redirect:/admin/expert-pending-list?approved";
    }

    @PostMapping("/expert-pending-list/reject")
    public String rejectExpert(@RequestParam int id, Model model){
        expertService.reject(id);
        model.addAttribute("successMessage", "Đã từ chối thành công!");
        return "redirect:/admin/expert-pending-list?rejected";
    }

    @GetMapping({"/escrow-reconciliation", "/revenue-reconciliation"})
    public String revenueReconciliation(@RequestParam(required = false) String orderId,
                                        @RequestParam(required = false) String sellerId,
                                        Model model) {
        Integer orderIdFilter = parseInteger(orderId);
        Integer sellerIdFilter = parseInteger(sellerId);
        Integer normalizedSellerId = resolveSellerIdForSettlementFilter(sellerIdFilter);
        List<OrderSettlement> settlements = orderSettlementRepository.findAll();
        if (orderIdFilter != null) {
            settlements = settlements.stream()
                    .filter(item -> orderIdFilter.equals(item.getOrderId()))
                    .toList();
        }
        if (normalizedSellerId != null) {
            settlements = settlements.stream()
                    .filter(item -> normalizedSellerId.equals(item.getSellerId()))
                    .toList();
        }
        Map<Integer, Object> orderDetailsByOrderId = new HashMap<>();
        for (OrderSettlement settlement : settlements) {
            if (settlement.getOrderId() == null || orderDetailsByOrderId.containsKey(settlement.getOrderId())) continue;
            try {
                orderDetailsByOrderId.put(settlement.getOrderId(), adminOrderService.getOrderDetail(settlement.getOrderId()));
            } catch (Exception ignored) {
            }
        }

        // Lấy tên seller
        Map<Integer, String> sellerNamesById = new HashMap<>();
        for (OrderSettlement settlement : settlements) {
            if (settlement.getSellerId() != null && !sellerNamesById.containsKey(settlement.getSellerId())) {
                try {
                    var seller = sellerRepository.findById(settlement.getSellerId());
                    if (seller.isPresent() && seller.get().getAccount() != null) {
                        var account = accountRepository.findById(seller.get().getAccount().getId());
                        if (account.isPresent()) {
                            sellerNamesById.put(settlement.getSellerId(), account.get().getUsername());
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        model.addAttribute("activePage", "revenue-reconciliation");
        model.addAttribute("adminWallet", walletBalanceRepository.findByAccountId(resolveAdminAccountId()).orElse(null));
        model.addAttribute("orderIdFilter", orderIdFilter);
        model.addAttribute("sellerIdFilter", sellerIdFilter);
        model.addAttribute("settlementRows", settlements);
        model.addAttribute("orderDetailsByOrderId", orderDetailsByOrderId);
        model.addAttribute("sellerNamesById", sellerNamesById);
        model.addAttribute("bankConfig", paymentBankConfigService.getActiveConfig());
        return "/pages/admin/escrow-reconciliation";
    }

    @GetMapping("/settlements/{orderId}")
    public String settlementDetail(@RequestParam(required = false) Integer orderId, Model model) {
        List<OrderSettlement> settlements = orderId != null ? orderSettlementRepository.findByOrderId(orderId) : orderSettlementRepository.findAll();
        model.addAttribute("activePage", "revenue-reconciliation");
        model.addAttribute("orderId", orderId);
        model.addAttribute("settlements", settlements);
        model.addAttribute("totalCommissionAmount", settlements.stream().map(s -> s.getCommissionAmount() != null ? s.getCommissionAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("totalSellerAmount", settlements.stream().map(s -> s.getSellerAmount() != null ? s.getSellerAmount() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("escrow", settlements.isEmpty() ? null : settlements.get(0));
        return "/pages/admin/settlement-detail";
    }

    @GetMapping("/seller-wallet")
    public String sellerWallet(@RequestParam(required = false) Integer sellerId, Model model) {
        model.addAttribute("activePage", "seller-wallet");
        model.addAttribute("sellerId", sellerId);
        model.addAttribute("sellerAccount", sellerId != null ? accountRepository.findById(sellerId).orElse(null) : null);
        model.addAttribute("wallet", sellerId != null ? walletBalanceRepository.findByAccountId(sellerId).orElse(null) : null);
        model.addAttribute("settlements", sellerId != null ? orderSettlementRepository.findBySellerId(sellerId) : List.of());
        return "/pages/seller/wallet";
    }

    @GetMapping("/users")
    public String showUserManagement(Model model) {
        model.addAttribute("activePage", "users");
        return "/pages/admin/user-management";
    }

    @GetMapping("/bank-config")
    public String bankConfig(Model model) {
        model.addAttribute("activePage", "bank-config");
        model.addAttribute("bankConfig", paymentBankConfigService.getActiveConfig());
        return "/pages/admin/bank-config";
    }

    @PostMapping("/bank-config")
    public String saveBankConfig(@RequestParam String bankName,
                                 @RequestParam String accountNumber,
                                 @RequestParam String accountHolderName,
                                 @RequestParam(required = false) String bankCode,
                                 Model model) {
        PaymentBankConfig config = paymentBankConfigService.getActiveConfig();
        if (config == null) {
            config = new PaymentBankConfig();
            config.setStatus("ACTIVE");
        }
        config.setBankName(bankName);
        config.setAccountNumber(accountNumber);
        config.setAccountHolderName(accountHolderName);
        config.setBankCode(bankCode);
        config.setUpdatedAt(LocalDateTime.now());
        paymentBankConfigService.save(config);
        model.addAttribute("successMessage", "Đã lưu cấu hình ngân hàng trung gian.");
        model.addAttribute("bankConfig", config);
        model.addAttribute("activePage", "bank-config");
        return "/pages/admin/bank-config";
    }

    private Integer resolveAdminAccountId() {
        return accountRepository.findByUsername("admin") != null ? accountRepository.findByUsername("admin").getId() : 1;
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * URL co the truyen accountId (tu payout requester) hoac sellerId (NhaBanHang.id).
     * Ham nay chuan hoa ve sellerId de loc bang OrderSettlement.sellerId.
     */
    private Integer resolveSellerIdForSettlementFilter(Integer rawSellerOrAccountId) {
        if (rawSellerOrAccountId == null) return null;

        Account account = accountRepository.findById(rawSellerOrAccountId).orElse(null);
        if (account != null) {
            Seller byAccount = sellerRepository.findByAccount(account).orElse(null);
            if (byAccount != null) return byAccount.getId();
        }

        return rawSellerOrAccountId;
    }
}
