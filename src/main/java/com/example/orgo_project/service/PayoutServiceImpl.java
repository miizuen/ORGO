package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.AuditLog;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IAuditLogRepository;
import com.example.orgo_project.repository.ITransactionHistoryRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import com.example.orgo_project.repository.IWithdrawalRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PayoutServiceImpl implements PayoutService {

    @Autowired
    private IWithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    private IWalletBalanceRepository walletBalanceRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private IAuditLogRepository auditLogRepository;

    @Autowired
    private ITransactionHistoryRepository transactionHistoryRepository;

    @Override
    public List<WithdrawalRequest> findAll() {
        return withdrawalRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<WithdrawalRequest> findPending() {
        return findByStatus(WithdrawalStatus.PENDING);
    }

    @Override
    public List<WithdrawalRequest> findByStatus(WithdrawalStatus status) {
        return withdrawalRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    public List<WithdrawalRequest> findByAccountId(Integer accountId) {
        return withdrawalRequestRepository.findByRequesterIdOrderByCreatedAtDesc(accountId);
    }

    @Override
    public List<WithdrawalRequest> findCurrentUserHistory() {
        Integer currentAccountId = getCurrentAccountId();
        if (currentAccountId == null) {
            return List.of();
        }
        return findByAccountId(currentAccountId);
    }

    @Override
    public List<TransactionHistory> findTransactionHistoryByAccountId(Integer accountId) {
        WalletBalance wallet = getWalletByAccountId(accountId);
        if (wallet == null) {
            return List.of();
        }
        return transactionHistoryRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    @Override
    public WithdrawalRequest findById(Integer id) {
        return withdrawalRequestRepository.findById(Objects.requireNonNull(id)).orElseThrow();
    }

    @Override
    public WalletBalance getWalletByAccountId(Integer accountId) {
        return walletBalanceRepository.findByAccountId(Objects.requireNonNull(accountId)).orElse(null);
    }

    @Override
    public WithdrawalRequest createRequest(Integer accountId, BigDecimal amount, String bankName, String bankAccount, String accountHolderName) {
        WalletBalance wallet = walletBalanceRepository.findByAccountId(Objects.requireNonNull(accountId))
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay vi cua tai khoan"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien rut khong hop le");
        }
        if (wallet.getAvailableBalance() == null || wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("So du khong du de rut");
        }

        if (wallet.getMaintenanceBalance() != null) {
            BigDecimal afterWithdrawal = wallet.getAvailableBalance().subtract(amount);
            if (afterWithdrawal.compareTo(wallet.getMaintenanceBalance()) < 0) {
                throw new IllegalArgumentException("Khong the rut tien vi se thap hon muc duy tri toi thieu " + wallet.getMaintenanceBalance() + " VND");
            }
        }

        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setHeldBalance((wallet.getHeldBalance() == null ? BigDecimal.ZERO : wallet.getHeldBalance()).add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletBalanceRepository.save(wallet);

        WithdrawalRequest request = new WithdrawalRequest();
        request.setRequestedAmount(amount);
        request.setActualAmount(amount);
        request.setBankName(bankName);
        request.setRecipientBankAccount(bankAccount);
        request.setAccountHolderName(accountHolderName);
        request.setStatus(WithdrawalStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setRequesterId(accountId);
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        writeAudit("WithdrawalRequest", saved.getId(), "CREATE", "Tao lenh rut tien so tien " + amount, accountId);
        writeAudit("WalletBalance", wallet.getId(), "LOCK", "Khoa so tien " + amount + " cho lenh rut #" + saved.getId(), accountId);

        return saved;
    }

    @Override
    public WithdrawalRequest approve(Integer id, String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            throw new IllegalArgumentException("Vui long nhap ma giao dich ngan hang.");
        }

        WithdrawalRequest request = findById(id);
        Integer ownerAccountId = request.getRequesterId();
        WalletBalance wallet = getWalletByAccountId(ownerAccountId);
        if (wallet == null) {
            throw new IllegalArgumentException("Khong tim thay vi cua tai khoan");
        }

        BigDecimal amount = request.getRequestedAmount() == null ? BigDecimal.ZERO : request.getRequestedAmount();
        if (wallet.getHeldBalance() != null) {
            wallet.setHeldBalance(wallet.getHeldBalance().subtract(amount));
        }
        wallet.setTotalWithdrawn((wallet.getTotalWithdrawn() == null ? BigDecimal.ZERO : wallet.getTotalWithdrawn()).add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletBalanceRepository.save(wallet);

        request.setStatus(WithdrawalStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        request.setTransactionCode(transactionCode.trim());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        String txCode = transactionCode.trim();
        writeAudit("WithdrawalRequest", saved.getId(), "APPROVE", "Phe duyet lenh rut #" + saved.getId() + " - Ma GD: " + txCode, getCurrentAccountId());
        writeAudit("WalletBalance", wallet.getId(), "DEBIT", "Tru so tien " + amount + " khi duyet lenh rut #" + saved.getId(), getCurrentAccountId());
        writeTransactionHistory(wallet.getId(), "DEBIT", amount, wallet.getAvailableBalance(), saved.getId(), "Admin duyet payout, tru tien. Ma GD: " + txCode);
        return saved;
    }

    @Override
    public WithdrawalRequest reject(Integer id, String reason) {
        WithdrawalRequest request = findById(id);
        Integer ownerAccountId = request.getRequesterId();
        WalletBalance wallet = getWalletByAccountId(ownerAccountId);
        if (wallet != null) {
            BigDecimal amount = request.getRequestedAmount() == null ? BigDecimal.ZERO : request.getRequestedAmount();
            BigDecimal held = wallet.getHeldBalance() == null ? BigDecimal.ZERO : wallet.getHeldBalance();
            wallet.setHeldBalance(held.subtract(amount));
            wallet.setAvailableBalance((wallet.getAvailableBalance() == null ? BigDecimal.ZERO : wallet.getAvailableBalance()).add(amount));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletBalanceRepository.save(wallet);
            writeTransactionHistory(wallet.getId(), "UNLOCK", amount, wallet.getAvailableBalance(), request.getId(), "Admin tu choi payout va hoan tien ve vi");
        }

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);
        writeAudit("WithdrawalRequest", saved.getId(), "REJECT", "Tu choi lenh rut #" + saved.getId() + ". Ly do: " + reason, getCurrentAccountId());
        return saved;
    }

    @Override
    public WithdrawalRequest markPaid(Integer id) {
        WithdrawalRequest request = findById(id);
        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);
        writeAudit("WithdrawalRequest", saved.getId(), "PAID", "Da chi tra lenh rut #" + saved.getId(), getCurrentAccountId());
        return saved;
    }

    private void writeAudit(String entityType, Integer entityId, String action, String description, Integer actorId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setDescription(description);
        auditLog.setActorId(actorId);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    private void writeTransactionHistory(Integer walletId, String type, BigDecimal amount, BigDecimal balanceAfter, Integer referenceId, String description) {
        TransactionHistory history = new TransactionHistory();
        history.setWalletId(walletId);
        history.setType(type);
        history.setAmount(amount);
        history.setBalanceAfter(balanceAfter);
        history.setReferenceId(referenceId);
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        transactionHistoryRepository.save(history);
    }

    private Integer getCurrentAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.User user) {
            Account account = accountRepository.findByUsername(user.getUsername());
            return account != null ? account.getId() : null;
        }
        if (principal instanceof com.example.orgo_project.security.CustomUserDetails details) {
            return details.getAccount().getId();
        }
        return null;
    }
}
