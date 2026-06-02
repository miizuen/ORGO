package com.example.orgo_project.repository;

import com.example.orgo_project.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IOrderRepository extends JpaRepository<CustomerOrder, Integer> {
}
