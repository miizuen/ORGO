package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Integer> {
    List<ShoppingCartItem> findByCartId(Integer cartId);

    ShoppingCartItem findByCartIdAndProductVariantId(Integer cartId, Integer productVariantId);

    void deleteByCartId(Integer cartId);
}