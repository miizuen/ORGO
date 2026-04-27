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
@Table(name = "DonHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_don_hang")
    private Integer id;

    @Column(name = "id_nguoi_dung")
    private Integer userId;

    @Column(name = "id_nha_ban_hang")
    private Integer sellerId;

    @Column(name = "id_dia_chi_giao_hang")
    private Integer shippingAddressId;

    @Column(name = "maDonHang")
    private String orderCode;

    @Column(name = "ngay_dat")
    private LocalDateTime orderedAt;

    @Column(name = "tong_tien")
    private BigDecimal totalAmount;

    @Column(name = "phi_van_chuyen")
    private BigDecimal shippingFee;

    @Column(name = "trang_thai_thanh_toan")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.PaymentStatus paymentStatus;

    @Column(name = "trang_thai_don_hang")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.OrderStatus orderStatus;

    @Column(name = "ghi_chu")
    private String note;

    @Column(name = "id_phuong_thuc")
    private Integer paymentMethodId;

    @Column(name = "ly_do_huy")
    private String cancellationReason;
}
