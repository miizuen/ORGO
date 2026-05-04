package com.example.orgo_project.repository;

import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICustomerOrderRepository extends JpaRepository<CustomerOrder, Integer> {
    List<CustomerOrder> findByUserIdOrderByOrderedAtDesc(Integer userId);

    List<CustomerOrder> findByUserIdAndOrderStatusOrderByOrderedAtDesc(Integer userId, OrderStatus status);
}