package com.example.orgo_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "DonHangChiTiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_don_hang-chi_tiet")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "id_bien_the_san_pham")
    private Integer productVariantId;

    @Column(name = "so_luong")
    private Integer quantity;

    @Column(name = "don_gia")
    private BigDecimal unitPrice;

    @Column(name = "thanh_tien")
    private BigDecimal lineTotal;
}
