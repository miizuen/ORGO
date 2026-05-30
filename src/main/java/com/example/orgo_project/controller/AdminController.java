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

        // Doanh thu / đơn hàng / lợi nhuận theo 12 tháng gần nhất
        List<com.example.orgo_project.dto.OrderSummaryDTO> eligibleRevenueOrders = allOrders.stream()
                .filter(o -> ("PAID".equals(o.getPaymentStatus()) || "COMPLETED".equals(o.getOrderStatus())) && o.getOrderedAt() != null)
                .toList();

        YearMonth current = YearMonth.from(LocalDate.now());
        List<YearMonth> months = new ArrayList<>();
        Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> profitByMonth = new LinkedHashMap<>();
        Map<YearMonth, Long> orderCountByMonth = new LinkedHashMap<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            months.add(ym);
            revenueByMonth.put(ym, BigDecimal.ZERO);
            profitByMonth.put(ym, BigDecimal.ZERO);
            orderCountByMonth.put(ym, 0L);
        }
        for (var o : eligibleRevenueOrders) {
            YearMonth ym = YearMonth.from(o.getOrderedAt().toLocalDate());
            if (revenueByMonth.containsKey(ym)) {
                BigDecimal amt = o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO;
                revenueByMonth.put(ym, revenueByMonth.get(ym).add(amt));
                orderCountByMonth.put(ym, orderCountByMonth.get(ym) + 1);
            }
        }
        for (OrderSettlement settlement : allSettlements) {
            LocalDateTime createdAt = settlement.getCreatedAt();
            if (createdAt == null) continue;
            YearMonth ym = YearMonth.from(createdAt.toLocalDate());
            if (profitByMonth.containsKey(ym)) {
                BigDecimal commission = settlement.getCommissionAmount() != null ? settlement.getCommissionAmount() : BigDecimal.ZERO;
                profitByMonth.put(ym, profitByMonth.get(ym).add(commission));
            }
        }

        List<BigDecimal> revenueValues = months.stream().map(revenueByMonth::get).toList();
        List<BigDecimal> profitValues = months.stream().map(profitByMonth::get).toList();
        List<BigDecimal> orderValues = months.stream().map(m -> BigDecimal.valueOf(orderCountByMonth.get(m))).toList();

        BigDecimal revenuePreviousMonth = revenueByMonth.get(current.minusMonths(1));
        BigDecimal revenueBeforePreviousMonth = revenueByMonth.get(current.minusMonths(2));
        long ordersPreviousMonth = orderCountByMonth.get(current.minusMonths(1));
        long ordersBeforePreviousMonth = orderCountByMonth.get(current.minusMonths(2));
        BigDecimal profitPreviousMonth = profitByMonth.get(current.minusMonths(1));
        BigDecimal profitBeforePreviousMonth = profitByMonth.get(current.minusMonths(2));

        String revenueTrendLabel = buildTrendLabel(revenueBeforePreviousMonth, revenuePreviousMonth, true);
        String ordersTrendLabel = buildTrendLabel(BigDecimal.valueOf(ordersBeforePreviousMonth), BigDecimal.valueOf(ordersPreviousMonth), false);
        String profitTrendLabel = buildTrendLabel(profitBeforePreviousMonth, profitPreviousMonth, true);

        BigDecimal revenueMax = revenueValues.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        BigDecimal profitMax = profitValues.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        BigDecimal orderMax = orderValues.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE);
        if (revenueMax.compareTo(BigDecimal.ZERO) == 0) revenueMax = BigDecimal.ONE;
        if (profitMax.compareTo(BigDecimal.ZERO) == 0) profitMax = BigDecimal.ONE;
        if (orderMax.compareTo(BigDecimal.ZERO) == 0) orderMax = BigDecimal.ONE;

        // SVG viewBox 0 0 800 300: line grid y=60..240, đường cao nhất ~y=40
        int n = revenueValues.size();
        double xStep = n <= 1 ? 0 : 800.0 / (n - 1);
        int yMin = 40;
        int yMax = 240;
        int bottom = 300;

        String revenueLinePath = buildChartPath(revenueValues, revenueMax, xStep, yMin, yMax, bottom, false);
        String revenueAreaPath = buildChartPath(revenueValues, revenueMax, xStep, yMin, yMax, bottom, true);
        String orderLinePath = buildChartPath(orderValues, orderMax, xStep, yMin, yMax, bottom, false);
        String orderAreaPath = buildChartPath(orderValues, orderMax, xStep, yMin, yMax, bottom, true);
        String profitLinePath = buildChartPath(profitValues, profitMax, xStep, yMin, yMax, bottom, false);
        String profitAreaPath = buildChartPath(profitValues, profitMax, xStep, yMin, yMax, bottom, true);

        List<Map<String, Integer>> revenuePoints = buildChartPoints(revenueValues, revenueMax, xStep, yMin, yMax);
        List<Map<String, Integer>> orderPoints = buildChartPoints(orderValues, orderMax, xStep, yMin, yMax);
        List<Map<String, Integer>> profitPoints = buildChartPoints(profitValues, profitMax, xStep, yMin, yMax);

        List<String> revenueYAxisLabels = buildYAxisLabels(revenueMax, true);
        List<String> orderYAxisLabels = buildYAxisLabels(orderMax, false);
        List<String> profitYAxisLabels = buildYAxisLabels(profitMax, true);

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

        List<String> recentActivities = buildRecentActivities(recentOrders, pendingOrders, pendingSellerCount, pendingExpertCount, pendingArticleCount, recentOrders.size(), allSettlements.size());
        List<com.example.orgo_project.dto.OrderSummaryDTO> recentCompletedOrders = allOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getOrderStatus()) || "PAID".equals(o.getPaymentStatus()))
                .sorted((o1, o2) -> o2.getOrderedAt().compareTo(o1.getOrderedAt()))
                .limit(4)
                .toList();

        List<Map<String, Object>> trafficSources = buildTrafficSources(totalViews, userCount, sellerCount, expertCount);
        List<Map<String, Object>> monthlyGoals = buildMonthlyGoals(totalPlatformRevenue, userCount, orderCount, totalAdminCommission);
        List<Map<String, Object>> orderSummaryCards = buildOrderSummaryCards(pendingOrders, recentCompletedOrders, orderCount, pendingOrders.size(), totalPlatformRevenue);
        List<Map<String, Object>> systemHealthCards = buildSystemHealthCards(userCount, buyerCount, sellerCount, expertCount, productRepository.count(), pendingSellerCount, pendingExpertCount, pendingArticleCount);
        List<Map<String, Object>> revenueBreakdownCards = buildRevenueBreakdownCards(totalPlatformRevenue, totalAdminCommission, totalViews, buyerCount);
        List<Map<String, Object>> timelineCards = buildTimelineCards(recentActivities, pendingOrders, recentCompletedOrders);

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalPlatformRevenue", totalPlatformRevenue);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalViewsLabel", totalViewsLabel);
        model.addAttribute("revenueTrendLabel", revenueTrendLabel);
        model.addAttribute("ordersTrendLabel", ordersTrendLabel);
        model.addAttribute("profitTrendLabel", profitTrendLabel);
        model.addAttribute("chartRevenueLinePath", revenueLinePath);
        model.addAttribute("chartRevenueAreaPath", revenueAreaPath);
        model.addAttribute("chartOrderLinePath", orderLinePath);
        model.addAttribute("chartOrderAreaPath", orderAreaPath);
        model.addAttribute("chartProfitLinePath", profitLinePath);
        model.addAttribute("chartProfitAreaPath", profitAreaPath);
        model.addAttribute("chartRevenuePoints", revenuePoints);
        model.addAttribute("chartOrderPoints", orderPoints);
        model.addAttribute("chartProfitPoints", profitPoints);
        model.addAttribute("chartRevenueValues", revenueValues);
        model.addAttribute("chartOrderValues", orderValues);
        model.addAttribute("chartProfitValues", profitValues);
        model.addAttribute("chartRevenueYAxisLabels", revenueYAxisLabels);
        model.addAttribute("chartOrderYAxisLabels", orderYAxisLabels);
        model.addAttribute("chartProfitYAxisLabels", profitYAxisLabels);
        model.addAttribute("chartRevenueMaxLabel", formatCompactK(revenueMax.longValue()));
        model.addAttribute("chartOrderMaxLabel", String.valueOf(orderMax.longValue()));
        model.addAttribute("chartProfitMaxLabel", formatCompactK(profitMax.longValue()));
        model.addAttribute("chartRevenueSummaryLabel", formatCompactK(revenueMax.longValue()));
        model.addAttribute("chartOrderSummaryLabel", String.valueOf(orderMax.longValue()));
        model.addAttribute("chartProfitSummaryLabel", formatCompactK(profitMax.longValue()));
        model.addAttribute("revenueMiniBars", revenueMiniBars);
        model.addAttribute("orderMiniBars", orderMiniBars);
        model.addAttribute("chartMonthLabels", months.stream().map(YearMonth::toString).toList());
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
        model.addAttribute("recentCompletedOrders", recentCompletedOrders);
        model.addAttribute("recentActivities", recentActivities);
        model.addAttribute("trafficSources", trafficSources);
        model.addAttribute("monthlyGoals", monthlyGoals);
        model.addAttribute("orderSummaryCards", orderSummaryCards);
        model.addAttribute("systemHealthCards", systemHealthCards);
        model.addAttribute("revenueBreakdownCards", revenueBreakdownCards);
        model.addAttribute("timelineCards", timelineCards);
        
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
                    int settlementSellerId = settlement.getSellerId();
                    var seller = sellerRepository.findById(settlementSellerId);
                    if (seller.isPresent() && seller.get().getAccount() != null) {
                        var account = accountRepository.findById(seller.get().getAccount().getId());
                        if (account.isPresent()) {
                            sellerNamesById.put(settlementSellerId, account.get().getUsername());
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

    private String buildChartPath(List<BigDecimal> values, BigDecimal maxValue, double xStep, int yMin, int yMax, int bottom, boolean closeArea) {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            int x = (int) Math.round(i * xStep);
            double ratio = values.get(i).doubleValue() / maxValue.doubleValue();
            int y = (int) Math.round(yMax - ratio * (yMax - yMin));
            if (i == 0) {
                path.append("M ").append(x).append(" ").append(y);
            } else {
                path.append(" L ").append(x).append(" ").append(y);
            }
        }
        if (closeArea && !values.isEmpty()) {
            int xLast = (int) Math.round((values.size() - 1) * xStep);
            path.append(" L ").append(xLast).append(" ").append(bottom)
                .append(" L 0 ").append(bottom)
                .append(" Z");
        }
        return path.toString();
    }

    private List<Map<String, Integer>> buildChartPoints(List<BigDecimal> values, BigDecimal maxValue, double xStep, int yMin, int yMax) {
        List<Map<String, Integer>> points = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            int x = (int) Math.round(i * xStep);
            double ratio = values.get(i).doubleValue() / maxValue.doubleValue();
            int y = (int) Math.round(yMax - ratio * (yMax - yMin));
            Map<String, Integer> point = new HashMap<>();
            point.put("x", x);
            point.put("y", y);
            points.add(point);
        }
        return points;
    }

    private List<String> buildYAxisLabels(BigDecimal maxValue, boolean currency) {
        BigDecimal safeMax = maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0 ? maxValue : BigDecimal.ONE;
        List<String> labels = new ArrayList<>();
        labels.add(formatChartAxisValue(safeMax, currency));
        labels.add(formatChartAxisValue(safeMax.multiply(BigDecimal.valueOf(0.75)), currency));
        labels.add(formatChartAxisValue(safeMax.multiply(BigDecimal.valueOf(0.50)), currency));
        labels.add(formatChartAxisValue(safeMax.multiply(BigDecimal.valueOf(0.25)), currency));
        labels.add("0");
        return labels;
    }

    private String formatChartAxisValue(BigDecimal value, boolean currency) {
        if (value == null) return "0";
        if (currency) {
            return formatCompactK(Math.max(0L, value.longValue()));
        }
        long rounded = Math.max(0L, value.setScale(0, java.math.RoundingMode.HALF_UP).longValue());
        return String.valueOf(rounded);
    }

    private List<Map<String, Object>> buildOrderSummaryCards(List<com.example.orgo_project.dto.OrderSummaryDTO> pendingOrders,
                                                            List<com.example.orgo_project.dto.OrderSummaryDTO> recentCompletedOrders,
                                                            int orderCount,
                                                            int pendingOrderCount,
                                                            BigDecimal totalRevenue) {
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(summaryCard("Đơn chờ xử lý", pendingOrderCount, "Đơn đang ở trạng thái PAID/PENDING", "#16a34a"));
        cards.add(summaryCard("Đơn hoàn tất gần đây", recentCompletedOrders.size(), "Danh sách 4 đơn mới nhất đã hoàn thành", "#3b82f6"));
        cards.add(summaryCard("Tổng đơn hàng", orderCount, "Tất cả đơn hàng đã ghi nhận trong hệ thống", "#f59e0b"));
        cards.add(summaryCard("Tổng doanh thu", totalRevenue != null ? totalRevenue.longValue() : 0L, "Doanh thu tích luỹ của nền tảng", "#8b5cf6"));
        return cards;
    }

    private List<Map<String, Object>> buildSystemHealthCards(long userCount,
                                                             long buyerCount,
                                                             long sellerCount,
                                                             long expertCount,
                                                             long productCount,
                                                             int pendingSellerCount,
                                                             int pendingExpertCount,
                                                             int pendingArticleCount) {
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(summaryCard("Người dùng", userCount, "Tổng tài khoản đang hoạt động", "#16a34a"));
        cards.add(summaryCard("Người mua", buyerCount, "Tài khoản khách hàng và người mua", "#3b82f6"));
        cards.add(summaryCard("Người bán", sellerCount, "Tài khoản bán hàng đang đăng ký", "#f59e0b"));
        cards.add(summaryCard("Chuyên gia", expertCount, "Tài khoản chuyên gia trong hệ thống", "#8b5cf6"));
        cards.add(summaryCard("Sản phẩm", productCount, "Số lượng sản phẩm hiện có", "#0f766e"));
        cards.add(summaryCard("Chờ duyệt", pendingSellerCount + pendingExpertCount + pendingArticleCount, "Tổng hồ sơ chờ xử lý", "#dc2626"));
        return cards;
    }

    private List<Map<String, Object>> buildRevenueBreakdownCards(BigDecimal totalRevenue,
                                                                 BigDecimal totalAdminCommission,
                                                                 long totalViews,
                                                                 long buyerCount) {
        List<Map<String, Object>> cards = new ArrayList<>();
        cards.add(summaryCard("Doanh thu nền tảng", totalRevenue != null ? totalRevenue.longValue() : 0L, "Tổng doanh thu từ đơn hàng hợp lệ", "#16a34a"));
        cards.add(summaryCard("Hoa hồng đối soát", totalAdminCommission != null ? totalAdminCommission.longValue() : 0L, "Hoa hồng ghi nhận từ các settlement", "#f59e0b"));
        cards.add(summaryCard("Lượt xem nội dung", totalViews, "Tổng lượt xem các bài viết đã xuất bản", "#3b82f6"));
        cards.add(summaryCard("Tệp khách hàng", buyerCount, "Số tài khoản người mua khả dụng", "#8b5cf6"));
        return cards;
    }

    private List<Map<String, Object>> buildTimelineCards(List<String> recentActivities,
                                                         List<com.example.orgo_project.dto.OrderSummaryDTO> pendingOrders,
                                                         List<com.example.orgo_project.dto.OrderSummaryDTO> recentCompletedOrders) {
        List<Map<String, Object>> cards = new ArrayList<>();
        if (!recentActivities.isEmpty()) {
            cards.add(summaryCard(recentActivities.get(0), 1, "Hoạt động nổi bật gần nhất", "#16a34a"));
        }
        if (recentActivities.size() > 1) {
            cards.add(summaryCard(recentActivities.get(1), 1, "Hoạt động tiếp theo", "#3b82f6"));
        }
        if (!pendingOrders.isEmpty()) {
            cards.add(summaryCard("Có " + pendingOrders.size() + " đơn đang chờ xử lý", pendingOrders.size(), "Cần theo dõi trong luồng vận hành", "#f59e0b"));
        }
        if (!recentCompletedOrders.isEmpty()) {
            cards.add(summaryCard("Có " + recentCompletedOrders.size() + " đơn hoàn tất gần đây", recentCompletedOrders.size(), "Nhóm đơn hoàn thiện cuối cùng", "#8b5cf6"));
        }
        return cards;
    }

    private Map<String, Object> summaryCard(String label, long value, String meta, String color) {
        Map<String, Object> card = new HashMap<>();
        card.put("label", label);
        card.put("value", value);
        card.put("meta", meta);
        card.put("color", color);
        return card;
    }

    private String buildTrendLabel(BigDecimal previous, BigDecimal current, boolean currency) {
        if (previous == null || current == null) return "—";
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) return "0%";
            return "↑ New";
        }
        BigDecimal diff = current.subtract(previous);
        BigDecimal pct = diff.multiply(BigDecimal.valueOf(100)).divide(previous.abs(), 1, java.math.RoundingMode.HALF_UP);
        String prefix = diff.signum() >= 0 ? "↑ +" : "↓ ";
        String formatted = pct.abs().toPlainString() + "%";
        return prefix + formatted;
    }

    private List<String> buildRecentActivities(List<com.example.orgo_project.dto.OrderSummaryDTO> recentOrders,
                                               List<com.example.orgo_project.dto.OrderSummaryDTO> pendingOrders,
                                               int pendingSellerCount,
                                               int pendingExpertCount,
                                               int pendingArticleCount,
                                               int recentOrderCount,
                                               int settlementCount) {
        List<String> activities = new ArrayList<>();
        if (!recentOrders.isEmpty()) {
            var latest = recentOrders.get(0);
            activities.add("Đơn hàng mới #" + latest.getOrderCode() + " từ " + (latest.getCustomerName() != null ? latest.getCustomerName() : "khách hàng") + " đã được ghi nhận");
        }
        if (recentOrders.size() > 1) {
            var second = recentOrders.get(1);
            activities.add("Thanh toán gần nhất " + (second.getTotalAmount() != null ? formatCurrency(second.getTotalAmount()) : "0 đ") + " cho đơn #" + second.getOrderCode());
        }
        if (pendingSellerCount > 0) {
            activities.add("Có " + pendingSellerCount + " hồ sơ người bán đang chờ duyệt");
        }
        if (pendingExpertCount > 0) {
            activities.add("Có " + pendingExpertCount + " hồ sơ chuyên gia đang chờ duyệt");
        }
        if (pendingArticleCount > 0) {
            activities.add("Có " + pendingArticleCount + " bài viết đang chờ duyệt");
        }
        if (recentOrderCount > 0) {
            activities.add("Có " + recentOrderCount + " đơn hàng gần đây để theo dõi");
        }
        if (settlementCount > 0) {
            activities.add("Có " + settlementCount + " đối soát hoa hồng đã ghi nhận");
        }
        while (activities.size() < 4) activities.add("Chưa có thêm hoạt động mới từ hệ thống");
        return activities.stream().limit(4).toList();
    }

    private List<Map<String, Object>> buildTrafficSources(long totalViews, long userCount, long sellerCount, long expertCount) {
        long total = Math.max(1L, userCount + sellerCount + expertCount);
        List<Map<String, Object>> sources = new ArrayList<>();
        sources.add(trafficSource("Trực tiếp", userCount, total, "#16a34a"));
        sources.add(trafficSource("Tự nhiên", sellerCount, total, "#3b82f6"));
        sources.add(trafficSource("Giới thiệu", expertCount, total, "#f59e0b"));
        sources.add(trafficSource("Lượt xem nội dung", totalViews, Math.max(totalViews, 1L), "#8b5cf6"));
        return sources;
    }

    private Map<String, Object> trafficSource(String label, long value, long total, String color) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("percent", Math.round((value * 100.0) / total));
        item.put("color", color);
        return item;
    }

    private List<Map<String, Object>> buildMonthlyGoals(BigDecimal revenue, long userCount, int orderCount, BigDecimal commission) {
        List<Map<String, Object>> goals = new ArrayList<>();
        goals.add(goal("Doanh Thu Hàng Tháng", revenue, new BigDecimal("55000000"), "#16a34a"));
        goals.add(goal("Khách Hàng Mới", BigDecimal.valueOf(userCount), new BigDecimal("1000"), "#3b82f6"));
        goals.add(goal("Tổng Đơn Hàng", BigDecimal.valueOf(orderCount), new BigDecimal("500"), "#f59e0b"));
        goals.add(goal("Hoa Hồng Đối Soát", commission, new BigDecimal("10000000"), "#8b5cf6"));
        return goals;
    }

    private Map<String, Object> goal(String label, BigDecimal current, BigDecimal target, String color) {
        Map<String, Object> goal = new HashMap<>();
        BigDecimal safeCurrent = current != null ? current : BigDecimal.ZERO;
        BigDecimal safeTarget = target != null && target.compareTo(BigDecimal.ZERO) > 0 ? target : BigDecimal.ONE;
        int percent = safeCurrent.multiply(BigDecimal.valueOf(100)).divide(safeTarget, 0, java.math.RoundingMode.HALF_UP).intValue();
        percent = Math.max(0, Math.min(100, percent));
        goal.put("label", label);
        goal.put("current", safeCurrent);
        goal.put("target", safeTarget);
        goal.put("percent", percent);
        goal.put("color", color);
        return goal;
    }

    private String formatCurrency(BigDecimal value) {
        return String.format(java.util.Locale.US, "%s đ", value != null ? value.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString() : "0");
    }

    private String formatCompactK(long value) {
        if (value >= 1000) {
            double k = value / 1000.0;
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
