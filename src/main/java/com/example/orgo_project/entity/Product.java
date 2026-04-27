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

@Entity
@Table(name = "SanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_san_pham")
    private Integer id;

    @Column(name = "id_nha_ban_hang")
    private Integer sellerId;

    @Column(name = "id_danh_muc")
    private Integer categoryId;

    @Column(name = "ten_san_pham")
    private String productName;

    @Column(name = "slug")
    private String slug;

    @Column(name = "mo_ta_ngan")
    private String shortDescription;

    @Column(name = "mo_ta_chi_tiet")
    private String detailedDescription;

    @Column(name = "thanh_phan")
    private String ingredients;

    @Column(name = "nguon_goc")
    private String origin;

    @Column(name = "loi_ich_suc_khoe")
    private String healthBenefits;

    @Column(name = "huong_dan_su_dung")
    private String usageInstructions;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ProductStatus status;

    @Column(name = "sao_trung_binh")
    private Float averageRating;

    @Column(name = "tong_danh")
    private Integer totalReviews;
}
