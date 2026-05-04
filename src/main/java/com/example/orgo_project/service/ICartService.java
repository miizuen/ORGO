package com.example.orgo_project.service;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CartSummaryDTO;

import java.util.List;

public interface ICartService {
    CartSummaryDTO getMyCart(Integer accountId);

    CartItemDTO addItem(Integer accountId, Integer productVariantId, Integer quantity);

    CartItemDTO updateItem(Integer accountId, Integer cartItemId, Integer quantity);

    boolean removeItem(Integer accountId, Integer cartItemId);

    void clearCart(Integer accountId);

    List<CartItemDTO> getCartItems(Integer accountId);
}