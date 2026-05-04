package com.example.orgo_project.service;

import com.example.orgo_project.dto.CheckoutPageDataDTO;
import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;

public interface ICheckoutService {
    CheckoutPageDataDTO getCheckoutPageData(Integer accountId, String selectedItemIds);

    CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request, String selectedItemIds);
}
