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
@Table(name = "SoDuVi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vi")
    private Integer id;

    @Column(name = "id_tai_khoan")
    private Integer accountId;

    @Column(name = "so_du_kha_dung")
    private BigDecimal availableBalance;

    @Column(name = "so_du_tam_giu")
    private BigDecimal heldBalance;

    @Column(name = "tong_da_rut")
    private BigDecimal totalWithdrawn;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;
}
