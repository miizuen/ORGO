package com.example.orgo_project.service;

import com.example.orgo_project.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    Page<UserProfile> findAll(Pageable pageable);

    boolean save(UserProfile user);

    boolean update(UserProfile user);


    UserProfile findById(int id);

    UserProfile findByEmail(String email);
    UserProfile findByPhoneNumber(String phoneNumber);
    boolean generateAndSendOtp(String email);
    boolean verifyOtp(String email, String otp);
    void resetPassword(String email, String newPassword);
}
