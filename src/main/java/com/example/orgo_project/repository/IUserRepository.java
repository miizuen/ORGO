package com.example.orgo_project.repository;

import com.example.orgo_project.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends JpaRepository<UserProfile, Integer> {
    UserProfile findByEmail(String email);
    UserProfile findByPhoneNumber(String phoneNumber);
}
