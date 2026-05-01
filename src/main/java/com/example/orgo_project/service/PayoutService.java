package com.example.orgo_project.service;

import com.example.orgo_project.entity.TransactionHistory;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.util.List;

public interface PayoutService {
    List<WithdrawalRequest> findAll();
    List<WithdrawalRequest> findPending();
    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
    List<WithdrawalRequest> findByAccountId(Integer accountId);
    List<WithdrawalRequest> findCurrentUserHistory();
    List<TransactionHistory> findTransactionHistoryByAccountId(Integer accountId);
    WithdrawalRequest findById(Integer id);
    WalletBalance getWalletByAccountId(Integer accountId);
    WithdrawalRequest createRequest(Integer accountId, BigDecimal amount, String bankName, String bankAccount, String accountHolderName);
    WithdrawalRequest approve(Integer id);
    WithdrawalRequest reject(Integer id, String reason);
    WithdrawalRequest markPaid(Integer id);
}
