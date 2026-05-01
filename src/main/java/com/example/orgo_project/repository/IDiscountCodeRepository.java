package com.example.orgo_project.repository;

import com.example.orgo_project.entity.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDiscountCodeRepository extends JpaRepository<DiscountCode, Integer> {
    DiscountCode findByCodeIgnoreCase(String code);
}