package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Role;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.enums.SellerStatus;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IRoleRepository;
import com.example.orgo_project.repository.ISellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService implements ISellerService{

    @Autowired
    private ISellerRepository sellerRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public void register(Seller seller) {
        sellerRepository.save(seller);
    }

    @Override
    public List<Seller> getPendingList() {
        return sellerRepository.findByStatus(SellerStatus.PENDING);
    }

    @Override
    public Seller findById(int id) {
        return sellerRepository.findById(id).orElseThrow();
    }

    @Override
    public void approve(int id) {
        Seller seller = sellerRepository.findById(id).orElseThrow();
        seller.setStatus(SellerStatus.ACTIVE);
        sellerRepository.save(seller);

        Account account = seller.getAccount();
        Role sellerRole = roleRepository.findByRoleName(RoleName.SELLER);
        account.setRole(sellerRole);
        accountRepository.save(account);
        try {
            String toEmail = account.getUser().getEmail();
            String fullName = account.getUser().getFullName();
            emailService.sendSellerDecisionMail(
                    toEmail,
                    fullName,
                    "APPROVED",
                    "Chúc mừng bạn! Hồ sơ Seller của bạn đã được phê duyệt."
            );
        } catch (Exception e) {
            throw new RuntimeException("Đã duyệt Seller nhưng không gửi được email thông báo.", e);
        }
    }

    @Override
    public void reject(int id) {
        Seller seller = sellerRepository.findById(id).orElseThrow();
        seller.setStatus(SellerStatus.REJECTED);
        sellerRepository.save(seller);
        try {
            Account account = seller.getAccount();
            String toEmail = account.getUser().getEmail();
            String fullName = account.getUser().getFullName();
            emailService.sendSellerDecisionMail(
                    toEmail,
                    fullName,
                    "REJECTED",
                    "Rất tiếc, hồ sơ Seller của bạn chưa được chấp thuận."
            );
        } catch (Exception e) {
            throw new RuntimeException("Đã từ chối Seller nhưng không gửi được email thông báo.", e);
        }
    }

    @Override
    public boolean hasApplied(Account account) {
        return false;
    }
}
