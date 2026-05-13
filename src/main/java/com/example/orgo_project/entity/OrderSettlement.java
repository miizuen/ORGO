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
@Table(name = "DonHangThanhToan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSettlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_thanh_toan_don_hang")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "id_nguoi_ban")
    private Integer sellerId;

    @Column(name = "tong_tien_don_hang")
    private BigDecimal orderAmount;

    @Column(name = "phi_hoa_hong")
    private BigDecimal commissionAmount;

    @Column(name = "tien_nguoi_ban_nhan")
    private BigDecimal sellerAmount;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;
}
