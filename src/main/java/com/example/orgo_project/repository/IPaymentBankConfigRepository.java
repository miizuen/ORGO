package com.example.orgo_project.repository;

import com.example.orgo_project.entity.PaymentBankConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPaymentBankConfigRepository extends JpaRepository<PaymentBankConfig, Integer> {
    Optional<PaymentBankConfig> findFirstByStatusOrderByIdDesc(String status);
}
