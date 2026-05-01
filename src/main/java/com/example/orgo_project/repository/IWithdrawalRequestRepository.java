package com.example.orgo_project.repository;

import com.example.orgo_project.entity.WithdrawalRequest;
import com.example.orgo_project.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IWithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
    List<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status);
    List<WithdrawalRequest> findAllByOrderByCreatedAtDesc();
    List<WithdrawalRequest> findByProcessorIdOrderByCreatedAtDesc(Integer processorId);
}
