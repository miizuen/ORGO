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

import java.time.LocalDateTime;

@Entity
@Table(name = "CauHinhNganHangThanhToan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBankConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ten_ngan_hang", columnDefinition = "NVARCHAR(255)")
    private String bankName;

    @Column(name = "so_tai_khoan", columnDefinition = "NVARCHAR(255)")
    private String accountNumber;

    @Column(name = "chu_tai_khoan", columnDefinition = "NVARCHAR(255)")
    private String accountHolderName;

    @Column(name = "ma_ngan_hang", columnDefinition = "NVARCHAR(50)")
    private String bankCode;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;
}
