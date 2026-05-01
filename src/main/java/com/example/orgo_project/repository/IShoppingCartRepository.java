package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    ShoppingCart findByAccountId(Integer accountId);
}