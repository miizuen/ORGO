package com.example.orgo_project.service;

import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import com.example.orgo_project.repository.IWithdrawalRequestRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PayoutSeedDataService {

    @Autowired
    private IWalletBalanceRepository walletBalanceRepository;

    @Autowired
    private IWithdrawalRequestRepository withdrawalRequestRepository;

    @PostConstruct
    public void seedPayoutDemoData() {
        if (walletBalanceRepository.count() == 0) {
            walletBalanceRepository.saveAll(List.of(
                    createWallet(1, new BigDecimal("5200000"), new BigDecimal("500000"), new BigDecimal("0")),
                    createWallet(2, new BigDecimal("800000"), new BigDecimal("0"), new BigDecimal("200000")),
                    createWallet(3, new BigDecimal("1500000"), new BigDecimal("1500000"), new BigDecimal("0"))
            ));
        }

        if (withdrawalRequestRepository.count() == 0) {
            withdrawalRequestRepository.saveAll(List.of(
                    createRequest(1, new BigDecimal("500000"), "Vietcombank", "1234567890", "Nguyễn Văn A", WithdrawalStatus.PENDING),
                    createRequest(2, new BigDecimal("600000"), "BIDV", "1111222233", "Trần Thị B", WithdrawalStatus.COMPLETED),
                    createRequest(3, new BigDecimal("300000"), "ACB", "2222333344", "Lê Văn C", WithdrawalStatus.REJECTED),
                    createRequest(1, new BigDecimal("250000"), "MB Bank", "3333444455", "Nguyễn Văn A", WithdrawalStatus.APPROVED)
            ));
        }
    }

    private WalletBalance createWallet(Integer accountId, BigDecimal available, BigDecimal held, BigDecimal withdrawn) {
        WalletBalance wallet = new WalletBalance();
        wallet.setAccountId(accountId);
        wallet.setAvailableBalance(available);
        wallet.setHeldBalance(held);
        wallet.setTotalWithdrawn(withdrawn);
        wallet.setUpdatedAt(LocalDateTime.now());
        return wallet;
    }

    private WithdrawalRequest createRequest(Integer accountId, BigDecimal amount, String bankName, String bankAccount, String accountHolderName, WithdrawalStatus status) {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setRequestedAmount(amount);
        request.setActualAmount(amount);
        request.setBankName(bankName);
        request.setRecipientBankAccount(bankAccount);
        request.setAccountHolderName(accountHolderName);
        request.setStatus(status);
        request.setCreatedAt(LocalDateTime.now().minusDays(1));
        request.setProcessorId(accountId);
        if (status == WithdrawalStatus.COMPLETED || status == WithdrawalStatus.APPROVED || status == WithdrawalStatus.REJECTED) {
            request.setProcessedAt(LocalDateTime.now());
        }
        return request;
    }
}
