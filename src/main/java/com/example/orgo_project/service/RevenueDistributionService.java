package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Article;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.repository.ArticleRepository;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IExpertRepository;
import com.example.orgo_project.repository.IOrderSettlementRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.ITransactionHistoryRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RevenueDistributionService implements IRevenueDistributionService {

    private static final BigDecimal SELLER_RATIO = new BigDecimal("0.95");
    private static final BigDecimal ADMIN_RATIO_FULL = new BigDecimal("0.05");
    private static final BigDecimal ADMIN_RATIO_SHARED = new BigDecimal("0.03");
    private static final BigDecimal EXPERT_RATIO = new BigDecimal("0.02");

    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductRepository productRepository;
    private final IOrderSettlementRepository orderSettlementRepository;
    private final IWalletBalanceRepository walletBalanceRepository;
    private final ITransactionHistoryRepository transactionHistoryRepository;
    private final IAccountRepository accountRepository;
    private final ISellerRepository sellerRepository;
    private final ArticleRepository articleRepository;
    private final IExpertRepository expertRepository;

    public RevenueDistributionService(ICustomerOrderRepository orderRepository,
                                      ICustomerOrderItemRepository orderItemRepository,
                                      IProductVariantRepository productVariantRepository,
                                      IProductRepository productRepository,
                                      IOrderSettlementRepository orderSettlementRepository,
                                      IWalletBalanceRepository walletBalanceRepository,
                                      ITransactionHistoryRepository transactionHistoryRepository,
                                      IAccountRepository accountRepository,
                                      ISellerRepository sellerRepository,
                                      ArticleRepository articleRepository,
                                      IExpertRepository expertRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.orderSettlementRepository = orderSettlementRepository;
        this.walletBalanceRepository = walletBalanceRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.accountRepository = accountRepository;
        this.sellerRepository = sellerRepository;
        this.articleRepository = articleRepository;
        this.expertRepository = expertRepository;
    }

    @Override
    public void distributeForOrder(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay don hang"));
        if (order.getPaymentStatus() != PaymentStatus.PAID) throw new RuntimeException("Don hang chua thanh toan");
        if (order.getOrderStatus() != OrderStatus.DELIVERED) throw new RuntimeException("Chi chia tien khi don da giao thanh cong");
        if (!orderSettlementRepository.findByOrderId(orderId).isEmpty()) return;

        boolean hasArticle = order.getArticleId() != null;
        BigDecimal adminRatio = hasArticle ? ADMIN_RATIO_SHARED : ADMIN_RATIO_FULL;

        Map<Integer, BigDecimal> sellerTotals = buildSellerTotals(orderId);
        BigDecimal totalAdminRevenue = BigDecimal.ZERO;

        for (Map.Entry<Integer, BigDecimal> entry : sellerTotals.entrySet()) {
            Integer sellerId = entry.getKey();
            BigDecimal sellerOrderAmount = entry.getValue();
            BigDecimal sellerAmount = sellerOrderAmount.multiply(SELLER_RATIO);
            BigDecimal adminAmount = sellerOrderAmount.multiply(adminRatio);
            totalAdminRevenue = totalAdminRevenue.add(adminAmount);
            saveSettlement(orderId, sellerId, sellerOrderAmount, adminAmount, sellerAmount);
            Integer sellerAccountId = resolveSellerAccountId(sellerId);
            if (sellerAccountId != null) {
                creditWallet(sellerAccountId, sellerAmount, "SELLER_PAYOUT", orderId, "Nhan tien hang don " + order.getOrderCode());
            }
        }

        Integer adminAccountId = resolveAdminAccountId();
        if (adminAccountId != null && totalAdminRevenue.compareTo(BigDecimal.ZERO) > 0) {
            creditWallet(adminAccountId, totalAdminRevenue, "ADMIN_COMMISSION", orderId, "Hoa hong don " + order.getOrderCode());
        }

        if (hasArticle) {
            distributeExpertCommission(order);
        }
    }

    private void distributeExpertCommission(CustomerOrder order) {
        Article article = articleRepository.findById(order.getArticleId()).orElse(null);
        if (article == null || article.getExpertId() == null) return;

        Expert expert = expertRepository.findById(article.getExpertId()).orElse(null);
        if (expert == null) {
            expert = expertRepository.findByAccount_Id(article.getExpertId()).orElse(null);
        }

        if (expert == null || expert.getAccount() == null) {
            System.out.println("DEBUG: Khong tim thay expert voi id=" + article.getExpertId());
            return;
        }

        BigDecimal expertAmount = order.getTotalAmount()
                .multiply(EXPERT_RATIO)
                .setScale(0, RoundingMode.HALF_UP);
        if (expertAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        creditWallet(
                expert.getAccount().getId(),
                expertAmount,
                "EXPERT_COMMISSION",
                order.getId(),
                "Hoa hong bai viet #" + order.getArticleId() + " - don " + order.getOrderCode()
        );
    }

    private Integer resolveAdminAccountId() {
        Account admin = accountRepository.findFirstByRole_RoleName(RoleName.ADMIN).orElse(null);
        return admin != null ? admin.getId() : 1;
    }

    private Integer resolveSellerAccountId(Integer sellerId) {
        Seller seller = sellerRepository.findById(sellerId).orElse(null);
        if (seller == null || seller.getAccount() == null) return null;
        return seller.getAccount().getId();
    }

    private Map<Integer, BigDecimal> buildSellerTotals(Integer orderId) {
        Map<Integer, BigDecimal> sellerTotals = new HashMap<>();
        List<CustomerOrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (CustomerOrderItem item : orderItems) {
            Integer sellerId = resolveSellerId(item);
            if (sellerId == null) continue;
            BigDecimal lineTotal = item.getLineTotal() == null ? BigDecimal.ZERO : item.getLineTotal();
            sellerTotals.put(sellerId, sellerTotals.getOrDefault(sellerId, BigDecimal.ZERO).add(lineTotal));
        }
        return sellerTotals;
    }

    private Integer resolveSellerId(CustomerOrderItem item) {
        if (item.getProductVariantId() == null) return null;
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
        if (variant == null || variant.getProductId() == null) return null;
        Product product = productRepository.findById(variant.getProductId()).orElse(null);
        return product != null ? product.getSellerId() : null;
    }

    private void saveSettlement(Integer orderId, Integer sellerId, BigDecimal orderAmount, BigDecimal adminAmount, BigDecimal sellerAmount) {
        OrderSettlement settlement = new OrderSettlement();
        settlement.setOrderId(orderId);
        settlement.setSellerId(sellerId);
        settlement.setOrderAmount(orderAmount);
        settlement.setCommissionAmount(adminAmount);
        settlement.setSellerAmount(sellerAmount);
        settlement.setStatus("SETTLED");
        settlement.setCreatedAt(LocalDateTime.now());
        orderSettlementRepository.save(settlement);
    }

    private void creditWallet(Integer accountId, BigDecimal amount, String type, Integer referenceId, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return;
        WalletBalance wallet = walletBalanceRepository.findByAccountId(accountId).orElseGet(() -> {
            WalletBalance created = new WalletBalance();
            created.setAccountId(accountId);
            created.setAvailableBalance(BigDecimal.ZERO);
            created.setHeldBalance(BigDecimal.ZERO);
            created.setTotalWithdrawn(BigDecimal.ZERO);
            created.setMaintenanceBalance(BigDecimal.ZERO);
            created.setUpdatedAt(LocalDateTime.now());
            return created;
        });
        if (wallet.getAvailableBalance() == null) wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        WalletBalance saved = walletBalanceRepository.save(wallet);

        TransactionHistory history = new TransactionHistory();
        history.setWalletId(saved.getId());
        history.setType(type);
        history.setAmount(amount);
        history.setBalanceAfter(saved.getAvailableBalance());
        history.setReferenceId(referenceId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(history);
    }
}
