package com.example.orgo_project.service;

import com.example.orgo_project.entity.PaymentBankConfig;
import com.example.orgo_project.repository.IPaymentBankConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentBankConfigService {
    private final IPaymentBankConfigRepository repository;

    public PaymentBankConfigService(IPaymentBankConfigRepository repository) {
        this.repository = repository;
    }

    public PaymentBankConfig getActiveConfig() {
        return repository.findFirstByStatusOrderByIdDesc("ACTIVE").orElse(null);
    }

    public PaymentBankConfig save(PaymentBankConfig config) {
        return repository.save(config);
    }
}
