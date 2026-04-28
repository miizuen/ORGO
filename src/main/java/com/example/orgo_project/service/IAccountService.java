package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAccountService {
    Page<Account> findAll(Pageable pageable);

    boolean save(Account account);

    boolean update(Account account);

    boolean delete(int id);

    Account findById(int id);

    Account findByUsername(String username);

    boolean changePassword(Account account, String currentPassword, String newPassword);
}
