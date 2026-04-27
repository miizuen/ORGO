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
@Table(name = "LichSuThanhToan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lich_su_thanh_toan")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "id_phuong_thuc")
    private Integer paymentMethodId;

    @Column(name = "ma_giao_dich")
    private String transactionCode;

    @Column(name = "so_tien")
    private BigDecimal amount;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.PaymentStatus status;

    @Column(name = "ngay_giao_dich")
    private LocalDateTime transactionAt;

    @Column(name = "ghi_chu")
    private String note;
}
