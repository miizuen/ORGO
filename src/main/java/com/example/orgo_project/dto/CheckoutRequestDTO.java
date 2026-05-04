package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {
    private Integer shippingAddressId;
    private Integer paymentMethodId;
    private String shipperNote;
    private String shopNote;
}
