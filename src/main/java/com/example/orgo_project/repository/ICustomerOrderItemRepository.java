package com.example.orgo_project.repository;

import com.example.orgo_project.entity.CustomerOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICustomerOrderItemRepository extends JpaRepository<CustomerOrderItem, Integer> {
    List<CustomerOrderItem> findByOrderId(Integer orderId);
}