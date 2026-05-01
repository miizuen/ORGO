package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.AuditLog;
import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.repository.IAuditLogRepository;
import com.example.orgo_project.repository.IAccountRepository;
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
        return withdrawalRequestRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(req -> Objects.equals(req.getProcessorId(), accountId))
                .toList();
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
        TransactionHistory history = new TransactionHistory();
        history.setWalletId(wallet.getId());
        history.setType("SUMMARY");
        history.setAmount(wallet.getAvailableBalance());
        history.setBalanceAfter(wallet.getAvailableBalance());
        history.setDescription("Tổng quan giao dịch ví");
        history.setCreatedAt(LocalDateTime.now());
        return List.of(history);
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ví của tài khoản"));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút không hợp lệ");
        }
        if (wallet.getAvailableBalance() == null || wallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để rút");
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
        request.setProcessorId(accountId);
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);

        writeAudit("WithdrawalRequest", saved.getId(), "CREATE", "Tạo lệnh rút tiền số tiền " + amount, accountId);
        writeAudit("WalletBalance", wallet.getId(), "LOCK", "Khóa số tiền " + amount + " cho lệnh rút #" + saved.getId(), accountId);

        return saved;
    }

    @Override
    public WithdrawalRequest approve(Integer id) {
        WithdrawalRequest request = findById(id);
        request.setStatus(WithdrawalStatus.APPROVED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);
        writeAudit("WithdrawalRequest", saved.getId(), "APPROVE", "Phê duyệt lệnh rút tiền #" + saved.getId(), getCurrentAccountId());
        return saved;
    }

    @Override
    public WithdrawalRequest reject(Integer id, String reason) {
        WithdrawalRequest request = findById(id);
        request.setStatus(WithdrawalStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);
        writeAudit("WithdrawalRequest", saved.getId(), "REJECT", "Từ chối lệnh rút #" + saved.getId() + ". Lý do: " + reason, getCurrentAccountId());
        return saved;
    }

    @Override
    public WithdrawalRequest markPaid(Integer id) {
        WithdrawalRequest request = findById(id);
        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setProcessedAt(LocalDateTime.now());
        request.setProcessorId(getCurrentAccountId());
        WithdrawalRequest saved = withdrawalRequestRepository.save(request);
        writeAudit("WithdrawalRequest", saved.getId(), "PAID", "Đã chi trả lệnh rút #" + saved.getId(), getCurrentAccountId());
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
