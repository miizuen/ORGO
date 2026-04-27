package com.example.orgo_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DonHang_MaGiamGia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDiscount {
    @EmbeddedId
    private CustomerOrderDiscountId id;

    @Column(name = "so_tien_giam")
    private BigDecimal discountAmount;

    @Column(name = "ngay_ap_dung")
    private LocalDateTime appliedAt;
}
