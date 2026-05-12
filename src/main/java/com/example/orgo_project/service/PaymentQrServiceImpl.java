package com.example.orgo_project.service;

import com.example.orgo_project.entity.PaymentBankConfig;
import com.example.orgo_project.entity.PaymentQrSession;
import com.example.orgo_project.repository.IPaymentQrSessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentQrServiceImpl implements PaymentQrService {

    private final IPaymentQrSessionRepository paymentQrSessionRepository;
    private final PaymentBankConfigService paymentBankConfigService;

    public PaymentQrServiceImpl(IPaymentQrSessionRepository paymentQrSessionRepository,
                                PaymentBankConfigService paymentBankConfigService) {
        this.paymentQrSessionRepository = paymentQrSessionRepository;
        this.paymentBankConfigService = paymentBankConfigService;
    }

    @Override
    public PaymentQrSession createQrSession(Integer orderId, BigDecimal amount) {
        PaymentBankConfig config = paymentBankConfigService.getActiveConfig();
        if (config == null) throw new RuntimeException("Chưa cấu hình ngân hàng trung gian");
        PaymentQrSession session = new PaymentQrSession();
        session.setOrderId(orderId);
        session.setQrCodeValue("ORGO-QR-" + orderId);
        session.setBankName(config.getBankName());
        session.setAccountNumber(config.getAccountNumber());
        session.setAccountHolderName(config.getAccountHolderName());
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
