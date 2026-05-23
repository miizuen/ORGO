package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.EscrowBalance;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.WalletBalance;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IEscrowBalanceRepository;
import com.example.orgo_project.repository.IOrderSettlementRepository;
import com.example.orgo_project.repository.ITransactionHistoryRepository;
import com.example.orgo_project.repository.IWalletBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscrowServiceImplTest {

    @Mock
    private IEscrowBalanceRepository escrowBalanceRepository;

    @Mock
    private IWalletBalanceRepository walletBalanceRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private ITransactionHistoryRepository transactionHistoryRepository;

    @Mock
    private IOrderSettlementRepository orderSettlementRepository;

    @InjectMocks
    private EscrowServiceImpl escrowService;

    private EscrowBalance escrow;
    private WalletBalance sellerWallet;
    private WalletBalance adminWallet;
    private Account sellerAccount;
    private Account adminAccount;

    @BeforeEach
    void setUp() {
        escrow = new EscrowBalance();
        escrow.setId(1);
        escrow.setOrderId(1001);
        escrow.setHeldAmount(new BigDecimal("1000000"));
        escrow.setCommissionAmount(new BigDecimal("50000"));
        escrow.setSellerPayoutAmount(new BigDecimal("950000"));
        escrow.setAdminRevenueAmount(new BigDecimal("50000"));
        escrow.setStatus("PENDING");

        sellerWallet = new WalletBalance();
        sellerWallet.setId(11);
        sellerWallet.setAccountId(21);
        sellerWallet.setAvailableBalance(new BigDecimal("100000"));
        sellerWallet.setHeldBalance(BigDecimal.ZERO);
        sellerWallet.setTotalWithdrawn(BigDecimal.ZERO);

        adminWallet = new WalletBalance();
        adminWallet.setId(12);
        adminWallet.setAccountId(1);
        adminWallet.setAvailableBalance(new BigDecimal("500000"));
        adminWallet.setHeldBalance(BigDecimal.ZERO);
        adminWallet.setTotalWithdrawn(BigDecimal.ZERO);

        sellerAccount = new Account();
        sellerAccount.setId(21);

        adminAccount = new Account();
        adminAccount.setId(1);
    }

    @Test
    void createEscrowForOrder_shouldPersistEscrowAndReturnSavedEntity() {
        when(escrowBalanceRepository.save(any(EscrowBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EscrowBalance result = escrowService.createEscrowForOrder(
                2001,
                new BigDecimal("1200000"),
                new BigDecimal("60000"),
                new BigDecimal("1140000"),
                new BigDecimal("60000")
        );

        assertNotNull(result);
        assertEquals(2001, result.getOrderId());
        assertEquals(new BigDecimal("1200000"), result.getHeldAmount());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getTotalHeldAmount_shouldSumAllEscrows() {
        EscrowBalance e1 = new EscrowBalance();
        e1.setHeldAmount(new BigDecimal("100000"));
        EscrowBalance e2 = new EscrowBalance();
        e2.setHeldAmount(new BigDecimal("250000"));
        EscrowBalance e3 = new EscrowBalance();
        e3.setHeldAmount(null);

        when(escrowBalanceRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        assertEquals(new BigDecimal("350000"), escrowService.getTotalHeldAmount());
    }

    @Test
    void getTotalAdminRevenue_shouldSumAllAdminRevenues() {
        EscrowBalance e1 = new EscrowBalance();
        e1.setAdminRevenueAmount(new BigDecimal("10000"));
        EscrowBalance e2 = new EscrowBalance();
        e2.setAdminRevenueAmount(new BigDecimal("15000"));
        EscrowBalance e3 = new EscrowBalance();
        e3.setAdminRevenueAmount(null);

        when(escrowBalanceRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        assertEquals(new BigDecimal("25000"), escrowService.getTotalAdminRevenue());
    }

    @Test
    void settleOrder_shouldDistributeToSellerAndAdminAndRecordSettlement() {
        when(escrowBalanceRepository.findByOrderId(1001)).thenReturn(Optional.of(escrow));
        when(accountRepository.findById(21)).thenReturn(Optional.of(sellerAccount));
        when(accountRepository.findByUsername("admin")).thenReturn(adminAccount);
        when(walletBalanceRepository.findByAccountId(21)).thenReturn(Optional.of(sellerWallet));
        when(walletBalanceRepository.findByAccountId(1)).thenReturn(Optional.of(adminWallet));
        when(walletBalanceRepository.save(any(WalletBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(escrowBalanceRepository.save(any(EscrowBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderSettlementRepository.save(any(OrderSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EscrowBalance result = escrowService.settleOrder(1001, Map.of(21, new BigDecimal("1000000")));

        assertNotNull(result);
        assertEquals("SETTLED", result.getStatus());
        assertEquals(new BigDecimal("50000"), result.getCommissionAmount());
        assertEquals(new BigDecimal("950000"), result.getSellerPayoutAmount());
        assertEquals(new BigDecimal("1050000"), sellerWallet.getAvailableBalance());
        assertEquals(new BigDecimal("550000"), adminWallet.getAvailableBalance());
    }

    @Test
    void settleOrder_shouldHandleMultipleSellers() {
        WalletBalance sellerWallet2 = new WalletBalance();
        sellerWallet2.setId(13);
        sellerWallet2.setAccountId(22);
        sellerWallet2.setAvailableBalance(new BigDecimal("200000"));
        sellerWallet2.setHeldBalance(BigDecimal.ZERO);
        sellerWallet2.setTotalWithdrawn(BigDecimal.ZERO);

        Account sellerAccount2 = new Account();
        sellerAccount2.setId(22);

        when(escrowBalanceRepository.findByOrderId(1001)).thenReturn(Optional.of(escrow));
        when(accountRepository.findById(21)).thenReturn(Optional.of(sellerAccount));
        when(accountRepository.findById(22)).thenReturn(Optional.of(sellerAccount2));
        when(accountRepository.findByUsername("admin")).thenReturn(adminAccount);
        when(walletBalanceRepository.findByAccountId(21)).thenReturn(Optional.of(sellerWallet));
        when(walletBalanceRepository.findByAccountId(22)).thenReturn(Optional.of(sellerWallet2));
        when(walletBalanceRepository.findByAccountId(1)).thenReturn(Optional.of(adminWallet));
        when(walletBalanceRepository.save(any(WalletBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(escrowBalanceRepository.save(any(EscrowBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderSettlementRepository.save(any(OrderSettlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EscrowBalance result = escrowService.settleOrder(1001, Map.of(
                21, new BigDecimal("600000"),
                22, new BigDecimal("400000")
        ));

        assertNotNull(result);
        assertEquals(new BigDecimal("50000"), result.getCommissionAmount());
        assertEquals(new BigDecimal("950000"), result.getSellerPayoutAmount());
        assertEquals(new BigDecimal("670000"), sellerWallet.getAvailableBalance());
        assertEquals(new BigDecimal("580000"), sellerWallet2.getAvailableBalance());
        assertEquals(new BigDecimal("550000"), adminWallet.getAvailableBalance());
    }

    @Test
    void settleOrder_shouldThrowWhenEscrowNotFound() {
        when(escrowBalanceRepository.findByOrderId(9999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> escrowService.settleOrder(9999, Map.of(21, new BigDecimal("100000"))));
    }

    @Test
    void findAllSettlements_shouldReturnRepositoryData() {
        OrderSettlement settlement = new OrderSettlement();
        settlement.setOrderId(1001);
        when(orderSettlementRepository.findAll()).thenReturn(List.of(settlement));

        assertEquals(1, escrowService.findAllSettlements().size());
        assertEquals(1001, escrowService.findAllSettlements().get(0).getOrderId());
    }
}
