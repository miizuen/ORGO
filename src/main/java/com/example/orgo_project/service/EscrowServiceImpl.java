package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.EscrowBalance;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IEscrowBalanceRepository;
import com.example.orgo_project.repository.IOrderRepository;
import com.example.orgo_project.repository.IOrderSettlementRepository;
import com.example.orgo_project.repository.ITransactionHistoryRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EscrowServiceImpl implements EscrowService {

    @Autowired
    private IEscrowBalanceRepository escrowBalanceRepository;

    @Autowired
    private IWalletBalanceRepository walletBalanceRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private ITransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private IOrderSettlementRepository orderSettlementRepository;

    @Override
    public EscrowBalance createEscrowForOrder(Integer orderId, BigDecimal totalAmount, BigDecimal commissionAmount, BigDecimal sellerPayoutAmount, BigDecimal adminRevenueAmount) {
        EscrowBalance escrow = new EscrowBalance();
        escrow.setOrderId(orderId);
        escrow.setHeldAmount(totalAmount);
        escrow.setCommissionAmount(commissionAmount);
        escrow.setSellerPayoutAmount(sellerPayoutAmount);
        escrow.setAdminRevenueAmount(adminRevenueAmount);
        escrow.setStatus("PENDING");
        escrow.setCreatedAt(LocalDateTime.now());
        escrow.setUpdatedAt(LocalDateTime.now());
        EscrowBalance saved = escrowBalanceRepository.save(escrow);
        writeEscrowTransaction(orderId, totalAmount, "ESCROW_IN", "Nạp tiền vào escrow cho đơn #" + orderId);
        return saved;
    }

    @Override
    public EscrowBalance findByOrderId(Integer orderId) {
        return escrowBalanceRepository.findByOrderId(orderId).orElse(null);
    }

    @Override
    public List<EscrowBalance> findAll() {
        return escrowBalanceRepository.findAll();
    }

    @Override
    public BigDecimal getTotalHeldAmount() {
        return escrowBalanceRepository.findAll().stream()
                .map(EscrowBalance::getHeldAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalAdminRevenue() {
        return escrowBalanceRepository.findAll().stream()
                .map(EscrowBalance::getAdminRevenueAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<OrderSettlement> findAllSettlements() {
        return orderSettlementRepository.findAll();
    }

    @Override
    public EscrowBalance settleOrder(Integer orderId, Map<Integer, BigDecimal> sellerTotals) {
        EscrowBalance escrow = escrowBalanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy escrow cho đơn hàng"));

        BigDecimal commissionTotal = BigDecimal.ZERO;
        BigDecimal sellerPaidTotal = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> entry : sellerTotals.entrySet()) {
            Integer sellerId = entry.getKey();
            BigDecimal subtotal = entry.getValue() == null ? BigDecimal.ZERO : entry.getValue();
            BigDecimal commission = subtotal.multiply(new BigDecimal("0.05")).setScale(0, java.math.RoundingMode.HALF_UP);
            BigDecimal payout = subtotal.subtract(commission);
            commissionTotal = commissionTotal.add(commission);
            sellerPaidTotal = sellerPaidTotal.add(payout);
            creditSeller(sellerId, payout, orderId);
            recordSettlement(orderId, sellerId, subtotal, commission, payout);
        }

        creditAdmin(commissionTotal, orderId);

        escrow.setCommissionAmount(commissionTotal);
        escrow.setSellerPayoutAmount(sellerPaidTotal);
        escrow.setAdminRevenueAmount(commissionTotal);
        escrow.setStatus("SETTLED");
        escrow.setUpdatedAt(LocalDateTime.now());
        EscrowBalance saved = escrowBalanceRepository.save(escrow);
        writeEscrowTransaction(orderId, commissionTotal, "ESCROW_OUT", "Xử lý hoa hồng cho đơn #" + orderId);
        return saved;
    }

    private void creditSeller(Integer sellerId, BigDecimal amount, Integer orderId) {
        if (sellerId == null || amount == null) {
            return;
        }
        Account sellerAccount = accountRepository.findById(sellerId).orElse(null);
        if (sellerAccount == null) {
            return;
        }
        WalletBalance sellerWallet = walletBalanceRepository.findByAccountId(sellerAccount.getId()).orElseGet(() -> {
            WalletBalance wallet = new WalletBalance();
            wallet.setAccountId(sellerAccount.getId());
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setHeldBalance(BigDecimal.ZERO);
            wallet.setTotalWithdrawn(BigDecimal.ZERO);
            wallet.setUpdatedAt(LocalDateTime.now());
            return wallet;
        });
        BigDecimal current = sellerWallet.getAvailableBalance() == null ? BigDecimal.ZERO : sellerWallet.getAvailableBalance();
        sellerWallet.setAvailableBalance(current.add(amount));
        sellerWallet.setUpdatedAt(LocalDateTime.now());
        WalletBalance savedWallet = walletBalanceRepository.save(sellerWallet);
        writeWalletTransaction(savedWallet.getId(), "CREDIT", amount, savedWallet.getAvailableBalance(), orderId, "Cộng tiền seller #" + sellerId + " từ đơn #" + orderId);
    }

    private void creditAdmin(BigDecimal amount, Integer orderId) {
        Account adminAccount = accountRepository.findByUsername("admin");
        if (adminAccount == null || amount == null) {
            return;
        }
        WalletBalance adminWallet = walletBalanceRepository.findByAccountId(adminAccount.getId()).orElseGet(() -> {
            WalletBalance wallet = new WalletBalance();
            wallet.setAccountId(adminAccount.getId());
            wallet.setAvailableBalance(BigDecimal.ZERO);
            wallet.setHeldBalance(BigDecimal.ZERO);
            wallet.setTotalWithdrawn(BigDecimal.ZERO);
            wallet.setUpdatedAt(LocalDateTime.now());
            return wallet;
        });
        BigDecimal current = adminWallet.getAvailableBalance() == null ? BigDecimal.ZERO : adminWallet.getAvailableBalance();
        adminWallet.setAvailableBalance(current.add(amount));
        adminWallet.setUpdatedAt(LocalDateTime.now());
        WalletBalance savedWallet = walletBalanceRepository.save(adminWallet);
        writeWalletTransaction(savedWallet.getId(), "CREDIT", amount, savedWallet.getAvailableBalance(), orderId, "Cộng hoa hồng admin từ đơn #" + orderId);
    }

    private void recordSettlement(Integer orderId, Integer sellerId, BigDecimal orderAmount, BigDecimal commissionAmount, BigDecimal sellerAmount) {
        OrderSettlement settlement = new OrderSettlement();
        settlement.setOrderId(orderId);
        settlement.setSellerId(sellerId);
        settlement.setOrderAmount(orderAmount);
        settlement.setCommissionAmount(commissionAmount);
        settlement.setSellerAmount(sellerAmount);
        settlement.setStatus("SETTLED");
        settlement.setCreatedAt(LocalDateTime.now());
        orderSettlementRepository.save(settlement);
    }

    private void writeEscrowTransaction(Integer orderId, BigDecimal amount, String type, String description) {
        TransactionHistory history = new TransactionHistory();
        history.setWalletId(orderId);
        history.setType(type);
        history.setAmount(amount);
        history.setBalanceAfter(amount);
        history.setReferenceId(orderId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(history);
    }

    private void writeWalletTransaction(Integer walletId, String type, BigDecimal amount, BigDecimal balanceAfter, Integer orderId, String description) {
        TransactionHistory history = new TransactionHistory();
        history.setWalletId(walletId);
        history.setType(type);
        history.setAmount(amount);
        history.setBalanceAfter(balanceAfter);
        history.setReferenceId(orderId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(history);
    }
}
