package com.example.orgo_project.repository;

import com.example.orgo_project.entity.EscrowBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IEscrowBalanceRepository extends JpaRepository<EscrowBalance, Integer> {
    Optional<EscrowBalance> findByOrderId(Integer orderId);
}
