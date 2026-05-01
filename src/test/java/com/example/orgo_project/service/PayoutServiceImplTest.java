package com.example.orgo_project.service;

import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import com.example.orgo_project.repository.IAuditLogRepository;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import com.example.orgo_project.repository.IWithdrawalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutServiceImplTest {

    @Mock
    private IWithdrawalRequestRepository withdrawalRequestRepository;

    @Mock
    private IWalletBalanceRepository walletBalanceRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private IAuditLogRepository auditLogRepository;

    @InjectMocks
    private PayoutServiceImpl payoutService;

    private WalletBalance wallet;

    @BeforeEach
    void setUp() {
        wallet = new WalletBalance();
        wallet.setId(1);
        wallet.setAccountId(10);
        wallet.setAvailableBalance(new BigDecimal("1000000"));
        wallet.setHeldBalance(new BigDecimal("100000"));
        wallet.setTotalWithdrawn(BigDecimal.ZERO);
    }

    @Test
    void createRequest_shouldLockMoneyAndCreatePendingRequest() {
        when(walletBalanceRepository.findByAccountId(10)).thenReturn(Optional.of(wallet));
        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequest result = payoutService.createRequest(
                10,
                new BigDecimal("200000"),
                "Vietcombank",
                "1234567890",
                "Nguyen Van A"
        );

        assertNotNull(result);
        assertEquals(WithdrawalStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("800000"), wallet.getAvailableBalance());
        assertEquals(new BigDecimal("300000"), wallet.getHeldBalance());
    }

    @Test
    void approve_shouldChangeStatusToApproved() {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setId(1);
        request.setStatus(WithdrawalStatus.PENDING);
        request.setRequestedAmount(new BigDecimal("200000"));

        when(withdrawalRequestRepository.findById(1)).thenReturn(Optional.of(request));
        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequest result = payoutService.approve(1);

        assertEquals(WithdrawalStatus.APPROVED, result.getStatus());
    }

    @Test
    void reject_shouldChangeStatusToRejected() {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setId(2);
        request.setStatus(WithdrawalStatus.PENDING);

        when(withdrawalRequestRepository.findById(2)).thenReturn(Optional.of(request));
        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequest result = payoutService.reject(2, "Sai thông tin ngân hàng");

        assertEquals(WithdrawalStatus.REJECTED, result.getStatus());
        assertEquals("Sai thông tin ngân hàng", result.getRejectionReason());
    }

    @Test
    void markPaid_shouldChangeStatusToCompleted() {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setId(3);
        request.setStatus(WithdrawalStatus.APPROVED);

        when(withdrawalRequestRepository.findById(3)).thenReturn(Optional.of(request));
        when(withdrawalRequestRepository.save(any(WithdrawalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequest result = payoutService.markPaid(3);

        assertEquals(WithdrawalStatus.COMPLETED, result.getStatus());
    }
}
