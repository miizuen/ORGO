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
@Table(name = "GioHangChiTiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_gio_hang_chi_tiet")
    private Integer id;

    @Column(name = "id_gio_hang")
    private Integer cartId;

    @Column(name = "id_bien_the_san_pham")
    private Integer productVariantId;

    @Column(name = "so_luong")
    private Integer quantity;

    @Column(name = "gia_tam_tinh")
    private BigDecimal estimatedPrice;
}
