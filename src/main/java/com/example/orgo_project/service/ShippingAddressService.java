package com.example.orgo_project.service;

import com.example.orgo_project.entity.ShippingAddress;
import com.example.orgo_project.repository.IShippingAddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShippingAddressService implements IShippingAddressService {
    @Autowired
    private IShippingAddressRepository shippingAddressRepository;

    @Override
    public List<ShippingAddress> findByAccountId(Integer accountId) {
        return shippingAddressRepository.findByAccountIdOrderByDefaultAddressDescIdDesc(accountId);
    }

    @Override
    public ShippingAddress findById(Integer id) {
        return shippingAddressRepository.findById(id).orElse(null);
    }

    @Override
    public boolean save(ShippingAddress address) {
        return shippingAddressRepository.save(address) != null;
    }

    @Override
    public boolean update(ShippingAddress address) {
        return shippingAddressRepository.save(address) != null;
    }

    @Override
    public boolean delete(Integer id, Integer accountId) {
        ShippingAddress address = shippingAddressRepository.findById(id).orElse(null);
        if (address == null || !address.getAccountId().equals(accountId)) {
            return false;
        }
        shippingAddressRepository.delete(address);
        return true;
    }

    @Override
    public boolean existsByAccountId(Integer accountId) {
        return shippingAddressRepository.countByAccountId(accountId) > 0;
    }
}
