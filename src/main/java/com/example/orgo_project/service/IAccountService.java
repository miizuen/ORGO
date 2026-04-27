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


//    Page<AccountDetailDto> searchAll(String name, String username, String email, String phone, Pageable pageable);

    Account findByUsername(String username);
}
