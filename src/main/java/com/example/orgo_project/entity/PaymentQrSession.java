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
@Table(name = "PhienThanhToanQR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQrSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_phien_qr")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "ma_qr", columnDefinition = "NVARCHAR(255)")
    private String qrCodeValue;

    @Column(name = "so_tai_khoan", columnDefinition = "NVARCHAR(50)")
    private String accountNumber;

    @Column(name = "ten_ngan_hang", columnDefinition = "NVARCHAR(255)")
    private String bankName;

    @Column(name = "chu_tai_khoan", columnDefinition = "NVARCHAR(255)")
    private String accountHolderName;

    @Column(name = "so_tien")
    private BigDecimal amount;

    @Column(name = "noi_dung_chuyen_khoan", columnDefinition = "NVARCHAR(255)")
    private String transferContent;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;

    @Column(name = "ngay_het_han")
    private LocalDateTime expiresAt;
}
