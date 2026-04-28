package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IAccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);

    Optional<Account> findById(Integer id);
}
