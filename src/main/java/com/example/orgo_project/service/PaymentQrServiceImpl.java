package com.example.orgo_project.service;

import com.example.orgo_project.entity.PaymentQrSession;
import com.example.orgo_project.repository.IPaymentQrSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentQrServiceImpl implements PaymentQrService {

    @Autowired
    private IPaymentQrSessionRepository paymentQrSessionRepository;

    @Override
    public PaymentQrSession createQrSession(Integer orderId, BigDecimal amount) {
        PaymentQrSession session = new PaymentQrSession();
        session.setOrderId(orderId);
        session.setQrCodeValue("ORGO-QR-" + orderId);
        session.setBankName("ORGO Escrow Bank");
        session.setAccountNumber("0123456789");
        session.setAccountHolderName("ORGO Marketplace Escrow");
        session.setAmount(amount);
        session.setTransferContent("ORGO-ORDER-" + orderId);
        session.setStatus("PENDING");
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        return paymentQrSessionRepository.save(session);
    }

    @Override
    public PaymentQrSession findByOrderId(Integer orderId) {
        return paymentQrSessionRepository.findByOrderId(orderId).orElse(null);
    }
}
