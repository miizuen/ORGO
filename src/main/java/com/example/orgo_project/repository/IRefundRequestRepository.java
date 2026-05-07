package com.example.orgo_project.repository;

import com.example.orgo_project.entity.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRefundRequestRepository extends JpaRepository<RefundRequest, Integer> {
}