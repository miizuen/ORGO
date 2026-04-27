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
@Table(name = "LichSuGiaoDich")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_giao_dich")
    private Integer id;

    @Column(name = "id_vi")
    private Integer walletId;

    @Column(name = "loai_giao_dich")
    private String transactionType;

    @Column(name = "so_tien")
    private BigDecimal amount;

    @Column(name = "so_du_sau")
    private BigDecimal balanceAfter;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "id_lenh_rut")
    private Integer withdrawalRequestId;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;
}
