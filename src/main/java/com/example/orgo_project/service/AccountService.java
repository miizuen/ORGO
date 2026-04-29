package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.repository.IAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements IAccountService{
    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<Account> findAll(Pageable pageable) {
        return accountRepository.findAll(pageable);
    }

    @Override
    public boolean save(Account account) {
        return accountRepository.save(account) != null;
    }

    @Override
    public boolean update(Account account) {
        return accountRepository.save(account) != null;
    }

    @Override
    public boolean delete(int id) {
        if (!accountRepository.existsById(id)) {
            return false;
        }
        accountRepository.deleteById(id);
        return true;
    }

    @Override
    public Account findById(int id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public Account findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public boolean changePassword(Account account, String currentPassword, String newPassword) {
        if (account == null || currentPassword == null || newPassword == null) {
            return false;
        }
        if (!passwordEncoder.matches(currentPassword, account.getPassword())) {
            return false;
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
        return true;
    }
}
