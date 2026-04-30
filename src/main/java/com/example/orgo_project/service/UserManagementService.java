package com.example.orgo_project.service;

import com.example.orgo_project.dto.UserManagementResponse;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.UserStatus;
import com.example.orgo_project.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    
    private final UserProfileRepository userProfileRepository;
    
    public Page<UserManagementResponse> getAllUsers(Pageable pageable) {
        return userProfileRepository.findAll(pageable)
            .map(this::convertToResponse);
    }
    
    public Page<UserManagementResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        return userProfileRepository.findByStatus(status, pageable)
            .map(this::convertToResponse);
    }
    
    @Transactional
    public void banUser(Integer userId) {
        UserProfile user = userProfileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.BANNED);
        userProfileRepository.save(user);
    }
    
    @Transactional
    public void unbanUser(Integer userId) {
        UserProfile user = userProfileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.ACTIVE);
        userProfileRepository.save(user);
    }
    
    public UserManagementResponse getUserDetails(Integer userId) {
        UserProfile user = userProfileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }
    
    private UserManagementResponse convertToResponse(UserProfile user) {
        UserManagementResponse response = new UserManagementResponse();
        response.setId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setRole(user.getAccount() != null ? user.getAccount().getRole().toString() : "USER");
        return response;
    }
}
