package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Account;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;

@ReadingConverter
public interface IAccountRepository extends JpaRepository<Account, Integer> {
    Account findByUsername(String username);
}
