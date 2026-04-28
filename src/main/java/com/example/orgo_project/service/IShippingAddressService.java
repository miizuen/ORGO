package com.example.orgo_project.service;

import com.example.orgo_project.entity.ShippingAddress;

import java.util.List;

public interface IShippingAddressService {
    List<ShippingAddress> findByAccountId(Integer accountId);

    ShippingAddress findById(Integer id);

    boolean save(ShippingAddress address);

    boolean update(ShippingAddress address);

    boolean delete(Integer id, Integer accountId);

    boolean existsByAccountId(Integer accountId);
}
