package com.example.orgo_project.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.UserStatus;

@Repository
public interface IUserProfileRepository extends JpaRepository<UserProfile, Integer> {

    Page<UserProfile> findByStatus(UserStatus status, Pageable pageable);

    Optional<UserProfile> findByAccount_Id(Integer accountId);
}
