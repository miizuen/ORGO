package com.example.orgo_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailDTO {
    private Integer id;
    private String orderCode;
    private String orderStatus;
    private String paymentStatus;
    private Integer shippingAddressId;
    private Integer paymentMethodId;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private String note;
    private String cancellationReason;
    private LocalDateTime orderedAt;
    private List<OrderItemDTO> items;
}