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
@Table(name = "LenhRut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lenh_rut")
    private Integer id;

    @Column(name = "so_tien_rut")
    private BigDecimal requestedAmount;

    @Column(name = "so_tien_thuc_nhan")
    private BigDecimal actualAmount;

    @Column(name = "ten_ngan_hang")
    private String bankName;

    @Column(name = "so_tai_khoan_nhan")
    private String recipientBankAccount;

    @Column(name = "ten_chu_tai_khoan")
    private String accountHolderName;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.WithdrawalStatus status;

    @Column(name = "ly_do_tu_choi")
    private String rejectionReason;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;

    @Column(name = "ngay_xu_ly")
    private LocalDateTime processedAt;

    @Column(name = "id_nguoi_xu_ly")
    private Integer processorId;
}
