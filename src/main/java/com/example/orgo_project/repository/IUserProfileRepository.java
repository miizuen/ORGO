package com.example.orgo_project.repository;

import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Page<UserProfile> findByStatus(UserStatus status, Pageable pageable);

    Optional<UserProfile> findByAccountId(Integer accountId);
}
