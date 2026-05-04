package com.example.orgo_project.dto;

import com.example.orgo_project.entity.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutPageDataDTO {
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private List<ShippingAddress> shippingAddresses;
    private ShippingAddress defaultShippingAddress;
}
