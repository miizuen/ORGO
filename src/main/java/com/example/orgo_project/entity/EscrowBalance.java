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
@Table(name = "SoDuEscrow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escrow")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "tong_tien_ham_giu")
    private BigDecimal heldAmount;

    @Column(name = "phi_hoa_hong")
    private BigDecimal commissionAmount;

    @Column(name = "tien_nguoi_ban_nhan")
    private BigDecimal sellerPayoutAmount;

    @Column(name = "tien_admin_nhan")
    private BigDecimal adminRevenueAmount;

    @Column(name = "trang_thai", columnDefinition = "NVARCHAR(50)")
    private String status;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;
}
