package com.example.orgo_project.service;

import com.example.orgo_project.entity.EscrowBalance;
import com.example.orgo_project.repository.IEscrowBalanceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EscrowSeedDataService {

    @Autowired
    private IEscrowBalanceRepository escrowBalanceRepository;

    @PostConstruct
    public void seedEscrowDemoData() {
        if (escrowBalanceRepository.count() == 0) {
            escrowBalanceRepository.saveAll(java.util.List.of(
                    createEscrow(1, new BigDecimal("1000000"), new BigDecimal("50000"), new BigDecimal("950000"), new BigDecimal("50000"), "SETTLED"),
                    createEscrow(2, new BigDecimal("2000000"), new BigDecimal("100000"), new BigDecimal("1900000"), new BigDecimal("100000"), "SETTLED"),
                    createEscrow(3, new BigDecimal("500000"), new BigDecimal("25000"), new BigDecimal("475000"), new BigDecimal("25000"), "PENDING")
            ));
        }
    }

    private EscrowBalance createEscrow(Integer orderId, BigDecimal heldAmount, BigDecimal commission, BigDecimal sellerPayout, BigDecimal adminRevenue, String status) {
        EscrowBalance escrow = new EscrowBalance();
        escrow.setOrderId(orderId);
        escrow.setHeldAmount(heldAmount);
        escrow.setCommissionAmount(commission);
        escrow.setSellerPayoutAmount(sellerPayout);
        escrow.setAdminRevenueAmount(adminRevenue);
        escrow.setStatus(status);
        escrow.setCreatedAt(LocalDateTime.now().minusDays(1));
        escrow.setUpdatedAt(LocalDateTime.now());
        return escrow;
    }
}