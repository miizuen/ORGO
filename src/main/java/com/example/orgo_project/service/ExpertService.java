package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.entity.Role;
import com.example.orgo_project.enums.ExpertStatus;
import com.example.orgo_project.enums.RoleName;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IExpertRepository;
import com.example.orgo_project.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpertService implements IExpertService{
    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private IExpertRepository expertRepository;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private EmailService emailService;


    @Override
    public void register(Expert expert) {
        expertRepository.save(expert);
    }

    @Override
    public List<Expert> getPendingList() {
        return expertRepository.findByStatus(ExpertStatus.PENDING);
    }

    @Override
    public Expert findById(int id) {
        return expertRepository.findById(id).orElseThrow();
    }

    @Override
    public void approve(int id) {
        Expert expert= expertRepository.findById(id).orElseThrow();
        expert.setStatus(ExpertStatus.ACTIVE);
        expertRepository.save(expert);

        Account account = expert.getAccount();
        Role roleName = roleRepository.findByRoleName(RoleName.EXPERT);
        account.setRole(roleName);
        accountRepository.save(account);

        try {
            String toEmail = account.getUser().getEmail();
            String fullName = account.getUser().getFullName();
            emailService.sendExpertDecisionEmail(
                    toEmail,
                    fullName,
                    "APPROVED",
                    "Chúc mừng bạn! Hồ sơ Expert của bạn đã được phê duyệt."
            );
        } catch (Exception e) {
            throw new RuntimeException("Đã duyệt Expert nhưng không gửi được email thông báo.", e);
        }
    }

    @Override
    public void reject(int id) {
        Expert expert = expertRepository.findById(id).orElseThrow();
        expert.setStatus(ExpertStatus.REJECTED);
        expertRepository.save(expert);
        try {
            Account account = expert.getAccount();
            String toEmail = account.getUser().getEmail();
            String fullName = account.getUser().getFullName();
            emailService.sendExpertDecisionEmail(
                    toEmail,
                    fullName,
                    "REJECTED",
                    "Rất tiếc, hồ sơ Expert của bạn chưa đáp ứng yêu cầu hiện tại."
            );
        } catch (Exception e) {
            throw new RuntimeException("Đã từ chối Expert nhưng không gửi được email thông báo.", e);
        }
    }

    @Override
    public boolean hasApplied(Account account) {
        return false;
    }
}
