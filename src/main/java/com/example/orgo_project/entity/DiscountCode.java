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
import java.time.LocalDateTime;

@Entity
@Table(name = "MaGiamGia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ma_giam_gia")
    private Integer id;

    @Column(name = "ma_code_giam_gia")
    private String code;

    @Column(name = "loai_giam_gia")
    private String discountType;

    @Column(name = "gia_tri_giam")
    private BigDecimal discountValue;

    @Column(name = "don_toi_thieu")
    private BigDecimal minimumOrderAmount;

    @Column(name = "so_luong_toi_da")
    private Integer maximumUsage;

    @Column(name = "so_luong_da_dung")
    private Integer usedCount;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime startsAt;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime endsAt;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.DiscountStatus status;
}
