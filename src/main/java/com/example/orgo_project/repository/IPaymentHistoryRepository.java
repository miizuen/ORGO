package com.example.orgo_project.repository;

import com.example.orgo_project.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPaymentHistoryRepository extends JpaRepository<PaymentHistory, Integer> {
}
