package com.example.orgo_project.service;

import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.repository.IUserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserProfileService {

    private final IUserProfileRepository userProfileRepository;

    public UserProfileService(IUserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Optional<UserProfile> findByAccountId(Integer accountId) {
        return userProfileRepository.findByAccountId(accountId);
    }

    public UserProfile save(UserProfile profile) {
        return userProfileRepository.save(profile);
    }
}
