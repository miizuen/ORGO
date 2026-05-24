package com.example.orgo_project.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.repository.IUserProfileRepository;

@Service
@Transactional
public class UserProfileService {

    private final IUserProfileRepository userProfileRepository;

    public UserProfileService(IUserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Optional<UserProfile> findByAccountId(Integer accountId) {
        return userProfileRepository.findByAccount_Id(accountId);
    }

    public UserProfile save(UserProfile profile) {
        return userProfileRepository.save(profile);
    }
}
