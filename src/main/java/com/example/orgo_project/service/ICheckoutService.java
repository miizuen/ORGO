package com.example.orgo_project.service;

import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;

public interface ICheckoutService {
    CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request);
}