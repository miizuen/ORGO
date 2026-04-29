package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.enums.ExpertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IExpertRepository extends JpaRepository<Expert, Integer> {
    List<Expert> findByStatus(ExpertStatus status);
    Optional<Expert> findByAccount(Account account);
}
