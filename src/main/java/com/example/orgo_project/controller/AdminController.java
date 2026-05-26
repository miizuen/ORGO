package com.example.orgo_project.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.example.orgo_project.entity.Article;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.PaymentBankConfig;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.enums.ArticleStatus;
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
                
        List<com.example.orgo_project.dto.OrderSummaryDTO> allOrders = adminOrderService.getAllOrders();
        BigDecimal totalPlatformRevenue = allOrders.stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()) || "COMPLETED".equals(o.getOrderStatus()))
                .map(com.example.orgo_project.dto.OrderSummaryDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int orderCount = allOrders.size();

        // Tổng lượt xem (từ BaiViet.luot_xem)
        List<Article> publishedArticles = articleRepository.findByStatus(
                ArticleStatus.PUBLISHED,
                org.springframework.data.domain.PageRequest.of(0, 2000)
        ).getContent();
        long totalViews = publishedArticles.stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount().longValue() : 0L)
                .sum();

        String totalViewsLabel = formatCompactK(totalViews);

        // Doanh thu theo 12 tháng gần nhất
        List<com.example.orgo_project.dto.OrderSummaryDTO> eligibleRevenueOrders = allOrders.stream()
                .filter(o -> ("PAID".equals(o.getPaymentStatus()) || "COMPLETED".equals(o.getOrderStatus())) && o.getOrderedAt() != null)
                .toList();

        YearMonth current = YearMonth.from(LocalDate.now());
        List<YearMonth> months = new ArrayList<>();
        Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            months.add(ym);
            revenueByMonth.put(ym, BigDecimal.ZERO);
        }
        for (var o : eligibleRevenueOrders) {
            YearMonth ym = YearMonth.from(o.getOrderedAt().toLocalDate());
            if (revenueByMonth.containsKey(ym)) {
                BigDecimal amt = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
                revenueByMonth.put(ym, revenueByMonth.get(ym).add(amt));
            }
        }

        List<BigDecimal> revenueValues = months.stream().map(revenueByMonth::get).toList();
        BigDecimal revenueMax = revenueValues.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        if (revenueMax.compareTo(BigDecimal.ZERO) == 0) revenueMax = BigDecimal.ONE;

        // SVG viewBox 0 0 800 300: line grid y=60..240, đường cao nhất ~y=40
        int n = revenueValues.size();
        double xStep = n <= 1 ? 0 : 800.0 / (n - 1);
        int yMin = 40;
        int yMax = 240;
        int bottom = 300;

        List<Integer> revenueYs = new ArrayList<>(n);
        for (BigDecimal v : revenueValues) {
            double ratio = v.doubleValue() / revenueMax.doubleValue();
            int y = (int) Math.round(yMax - ratio * (yMax - yMin));
            revenueYs.add(y);
        }

        StringBuilder chartLinePath = new StringBuilder();
        StringBuilder chartAreaPath = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int x = (int) Math.round(i * xStep);
            int y = revenueYs.get(i);
            if (i == 0) {
                chartLinePath.append("M ").append(x).append(" ").append(y);
                chartAreaPath.append("M ").append(x).append(" ").append(y);
            } else {
                chartLinePath.append(" L ").append(x).append(" ").append(y);
                chartAreaPath.append(" L ").append(x).append(" ").append(y);
            }
        }
        int xLast = (int) Math.round((n - 1) * xStep);
        int yLast = revenueYs.get(n - 1);
        chartAreaPath.append(" L ").append(xLast).append(" ").append(bottom)
                .append(" L 0 ").append(bottom)
                .append(" Z");

        // Mini chart (6 tháng gần nhất)
        int last6Start = Math.max(0, n - 6);
        BigDecimal revenueMax6 = revenueValues.subList(last6Start, n).stream()
                .max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        if (revenueMax6.compareTo(BigDecimal.ZERO) == 0) revenueMax6 = BigDecimal.ONE;
        final BigDecimal revenueMax6Final = revenueMax6;

        List<Integer> revenueMiniBars = revenueValues.subList(last6Start, n).stream()
                .map(v -> {
                    double ratio = v.doubleValue() / revenueMax6Final.doubleValue();
                    int pct = (int) Math.round(ratio * 100);
                    return Math.max(5, Math.min(100, pct));
                }).toList();

        // Mini chart số đơn hàng (6 tháng gần nhất)
        Map<YearMonth, Long> orderCountByMonth = new LinkedHashMap<>();
        for (YearMonth ym : months) orderCountByMonth.put(ym, 0L);
        for (var o : eligibleRevenueOrders) {
            YearMonth ym = YearMonth.from(o.getOrderedAt().toLocalDate());
            if (orderCountByMonth.containsKey(ym)) {
                orderCountByMonth.put(ym, orderCountByMonth.get(ym) + 1);
            }
        }
        List<Long> orderCountsValues = months.stream().map(orderCountByMonth::get).toList();
        long orderMax6 = orderCountsValues.subList(last6Start, n).stream().max(Long::compareTo).orElse(1L);
        if (orderMax6 == 0) orderMax6 = 1L;
        final long orderMax6Final = orderMax6;

        List<Integer> orderMiniBars = orderCountsValues.subList(last6Start, n).stream()
                .map(v -> {
                    double ratio = v.doubleValue() / (double) orderMax6Final;
                    int pct = (int) Math.round(ratio * 100);
                    return Math.max(5, Math.min(100, pct));
                }).toList();

        long userCount = accountRepository.count();
        long sellerCount = sellerRepository.count();
        long expertCount = expertRepository.count();
        long buyerCount = userCount - sellerCount - expertCount;
        if (buyerCount < 0) buyerCount = 0;

        int pendingSellerCount = sellerRepository.findByStatus(com.example.orgo_project.enums.SellerStatus.PENDING).size();
        int pendingExpertCount = expertRepository.findByStatus(com.example.orgo_project.enums.ExpertStatus.PENDING).size();
        int pendingArticleCount = (int) articleRepository.findByStatus(com.example.orgo_project.enums.ArticleStatus.PENDING, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
        
        List<com.example.orgo_project.dto.OrderSummaryDTO> recentOrders = allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderedAt().compareTo(o1.getOrderedAt()))
                .limit(5)
                .toList();

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalPlatformRevenue", totalPlatformRevenue);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalViewsLabel", totalViewsLabel);
        model.addAttribute("chartLinePath", chartLinePath.toString());
        model.addAttribute("chartAreaPath", chartAreaPath.toString());
        model.addAttribute("revenueMiniBars", revenueMiniBars);
        model.addAttribute("orderMiniBars", orderMiniBars);
        model.addAttribute("chartMonthLabels", List.of("Th1", "Th2", "Th3", "Th4", "Th5", "Th6", "Th7", "Th8", "Th9", "Th10", "Th11", "Th12"));
        model.addAttribute("userCount", userCount);
        model.addAttribute("buyerCount", buyerCount);
        model.addAttribute("sellerCount", sellerCount);
        model.addAttribute("expertCount", expertCount);
        
        model.addAttribute("pendingSellerCount", pendingSellerCount);
        model.addAttribute("pendingExpertCount", pendingExpertCount);
        model.addAttribute("pendingArticleCount", pendingArticleCount);
        
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("pendingOrderCount", pendingOrders.size());
        model.addAttribute("orders", pendingOrders);
        model.addAttribute("recentOrders", recentOrders);
        
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

    private String formatCompactK(long value) {
        if (value >= 1000) {
            double k = value / 1000.0;
            // 1.2K, 284K...
            if (k >= 100) return ((long) k) + "K";
            return String.format(java.util.Locale.US, "%.1fK", k).replace(".0K", "K");
        }
        return String.valueOf(value);
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
