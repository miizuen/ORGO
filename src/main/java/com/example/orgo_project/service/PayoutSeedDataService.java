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
                    createWallet(1, new BigDecimal("50000000"), new BigDecimal("0"), new BigDecimal("0")),
                    createWallet(3, new BigDecimal("5200000"), new BigDecimal("500000"), new BigDecimal("0")),
                    createWallet(4, new BigDecimal("2800000"), new BigDecimal("0"), new BigDecimal("200000")),
                    createWallet(5, new BigDecimal("1500000"), new BigDecimal("1500000"), new BigDecimal("0"))
            ));
        }

        if (withdrawalRequestRepository.count() == 0) {
            withdrawalRequestRepository.saveAll(List.of(
                    createRequest(3, new BigDecimal("500000"), "Vietcombank", "1234567890", "Seller Demo", WithdrawalStatus.PENDING),
                    createRequest(4, new BigDecimal("600000"), "BIDV", "1111222233", "Expert Demo", WithdrawalStatus.COMPLETED),
                    createRequest(5, new BigDecimal("300000"), "ACB", "2222333344", "Seller Demo 2", WithdrawalStatus.REJECTED),
                    createRequest(3, new BigDecimal("250000"), "MB Bank", "3333444455", "Seller Demo", WithdrawalStatus.APPROVED)
            ));
        }
    }

    private WalletBalance createWallet(Integer accountId, BigDecimal available, BigDecimal held, BigDecimal withdrawn) {
        WalletBalance wallet = new WalletBalance();
        wallet.setAccountId(accountId);
        wallet.setAvailableBalance(available);
        wallet.setHeldBalance(held);
        wallet.setTotalWithdrawn(withdrawn);
        // Set maintenance balance for sellers (accounts 3,4,5 are sellers/experts)
        if (accountId >= 3) {
            wallet.setMaintenanceBalance(new BigDecimal("20000"));
        }
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
