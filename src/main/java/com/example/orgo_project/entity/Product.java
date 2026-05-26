package com.example.orgo_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    @Column(name = "ten_san_pham", columnDefinition = "NVARCHAR(255)")
    private String productName;

    @Column(name = "slug", columnDefinition = "NVARCHAR(255)")
    private String slug;

    @Column(name = "mo_ta_ngan", columnDefinition = "NVARCHAR(MAX)")
    private String shortDescription;

    @Column(name = "mo_ta_chi_tiet", columnDefinition = "NVARCHAR(MAX)")
    private String detailedDescription;

    @Column(name = "thanh_phan", columnDefinition = "NVARCHAR(MAX)")
    private String ingredients;

    @Column(name = "nguon_goc", columnDefinition = "NVARCHAR(255)")
    private String origin;

    @Column(name = "loi_ich_suc_khoe", columnDefinition = "NVARCHAR(MAX)")
    private String healthBenefits;

    @Column(name = "huong_dan_su_dung", columnDefinition = "NVARCHAR(MAX)")
    private String usageInstructions;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ProductStatus status;

    @Column(name = "an_san_pham")
    private Boolean hidden;

    @Column(name = "sao_trung_binh")
    private Float averageRating;

    @Column(name = "tong_danh")
    private Integer totalReviews;

    @Column(name = "hinh_anh", columnDefinition = "NVARCHAR(255)")
    private String imageUrl;

    @Transient
    private List<ProductVariant> variants;

    @Transient
    public java.math.BigDecimal getMinPrice() {
        if (variants == null || variants.isEmpty()) return java.math.BigDecimal.ZERO;
        java.math.BigDecimal min = null;
        for (ProductVariant v : variants) {
            java.math.BigDecimal price = v.getDiscountedPrice() != null ? v.getDiscountedPrice() : v.getOriginalPrice();
            if (price != null) {
                if (min == null || price.compareTo(min) < 0) {
                    min = price;
                }
            }
        }
        return min != null ? min : java.math.BigDecimal.ZERO;
    }

    @Transient
    public java.math.BigDecimal getMaxPrice() {
        if (variants == null || variants.isEmpty()) return java.math.BigDecimal.ZERO;
        java.math.BigDecimal max = null;
        for (ProductVariant v : variants) {
            java.math.BigDecimal price = v.getDiscountedPrice() != null ? v.getDiscountedPrice() : v.getOriginalPrice();
            if (price != null) {
                if (max == null || price.compareTo(max) > 0) {
                    max = price;
                }
            }
        }
        return max != null ? max : java.math.BigDecimal.ZERO;
    }

    @Transient
    public boolean isOnSale() {
        if (variants == null || variants.isEmpty()) return false;
        for (ProductVariant v : variants) {
            if (v.getDiscountedPrice() != null && v.getOriginalPrice() != null && v.getDiscountedPrice().compareTo(v.getOriginalPrice()) < 0) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isBestSeller() {
        return averageRating != null && averageRating >= 4.5f && totalReviews != null && totalReviews >= 5;
    }

    @Transient
    public boolean isNewProduct() {
        return id != null && (id % 3 == 0 || id > 10);
    }

    @Transient
    public int getTotalStock() {
        if (variants == null || variants.isEmpty()) return 0;
        int total = 0;
        for (ProductVariant variant : variants) {
            if (variant.getStockQuantity() != null) {
                total += variant.getStockQuantity();
            }
        }
        return total;
    }

    @PostLoad
    private void loadLegacyImageUrl() {
        if ((imageUrl == null || imageUrl.isBlank()) && slug != null && slug.startsWith("/uploads/")) {
            imageUrl = slug;
        }
    }

    @PrePersist
    @PreUpdate
    private void syncLegacyImageUrl() {
        if ((imageUrl == null || imageUrl.isBlank()) && slug != null && slug.startsWith("/uploads/")) {
            imageUrl = slug;
        }
    }
}
