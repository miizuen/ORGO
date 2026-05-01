package com.example.orgo_project.repository;

import com.example.orgo_project.entity.WalletBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IWalletBalanceRepository extends JpaRepository<WalletBalance, Integer> {
    Optional<WalletBalance> findByAccountId(Integer accountId);
    List<WalletBalance> findByAccountIdIn(Iterable<Integer> accountIds);
}
