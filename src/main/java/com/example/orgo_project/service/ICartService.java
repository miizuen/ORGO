package com.example.orgo_project.service;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CartSummaryDTO;

public interface ICartService {
    CartSummaryDTO getMyCart(Integer accountId);

    CartItemDTO addItem(Integer accountId, Integer productVariantId, Integer quantity);

    CartItemDTO updateItem(Integer accountId, Integer cartItemId, Integer quantity);

    boolean removeItem(Integer accountId, Integer cartItemId);

    void clearCart(Integer accountId);
}