package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.repository.IAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements IAccountService{
    @Autowired
    private IAccountRepository accountRepository;

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public boolean save(Account account) {
        return false;
    }

    @Override
    public boolean update(Account account) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public Account findById(int id) {
        return null;
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}
