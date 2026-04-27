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
@Table(name = "BienTheSanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bien_the_san_pham")
    private Integer id;

    @Column(name = "id_san_pham")
    private Integer productId;

    @Column(name = "ten_bien_the", columnDefinition = "NVARCHAR(255)")
    private String variantName;

    @Column(name = "khoi_luong")
    private BigDecimal weight;

    @Column(name = "gia_goc")
    private BigDecimal originalPrice;

    @Column(name = "gia_khuyen_mai")
    private BigDecimal discountedPrice;

    @Column(name = "so_luong_ton")
    private Integer stockQuantity;

    @Column(name = "hinh_anh", columnDefinition = "NVARCHAR(255)")
    private String imageUrl;
}
