package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IShippingAddressRepository extends JpaRepository<ShippingAddress, Integer> {
    List<ShippingAddress> findByAccountIdOrderByDefaultAddressDescIdDesc(Integer accountId);

    long countByAccountId(Integer accountId);

    List<ShippingAddress> findByAccountIdAndDefaultAddressTrue(Integer accountId);
}
