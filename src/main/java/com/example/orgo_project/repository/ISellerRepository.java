package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.enums.SellerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ISellerRepository extends JpaRepository<Seller, Integer> {
    List<Seller> findByStatus(SellerStatus status);
    Optional<Seller> findByAccount(Account account);
}
