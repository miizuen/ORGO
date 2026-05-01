package com.example.orgo_project.repository;

import com.example.orgo_project.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITransactionHistoryRepository extends JpaRepository<TransactionHistory, Integer> {
    List<TransactionHistory> findByWalletIdOrderByCreatedAtDesc(Integer walletId);
}
