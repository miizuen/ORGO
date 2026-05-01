package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private Integer id;
    private Integer productVariantId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}