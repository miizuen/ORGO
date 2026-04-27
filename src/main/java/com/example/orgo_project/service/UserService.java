package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.IUserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService implements IUserService{
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<UserProfile> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public boolean save(UserProfile user) {
        return userRepository.save(user) != null;
    }

    @Override
    public boolean update(UserProfile user) {
        return userRepository.save(user) != null;
    }


    @Override
    public UserProfile findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserProfile findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserProfile findByPhoneNumber(String phone) {
        return userRepository.findByPhoneNumber(phone);
    }

    @Override
    public boolean generateAndSendOtp(String email) {
        UserProfile user = userRepository.findByEmail(email);
        if(user == null){
            return false;
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        user.setResetOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        try {
            emailService.sendOtpMail(email,otp);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean verifyOtp(String email, String otp) {
        UserProfile user = userRepository.findByEmail(email);

        if(user == null || user.getResetOtp() == null){
            return false;
        }

        if(user.getResetOtp().equals(otp) && user.getOtpExpiry().isAfter(LocalDateTime.now())){
            return true;
        }
        return false;
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        UserProfile user = userRepository.findByEmail(email);
        if (user != null) {
            Account account = user.getAccount();
            if (account != null) {
                account.setPassword(passwordEncoder.encode(newPassword));
                accountRepository.save(account);
            }
            user.setResetOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
        }
    }
}
