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
@Table(name = "YeuCauHoanTra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "id_nguoi_dung")
    private Integer userId;

    @Column(name = "ly_do", columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    @Column(name = "anh_minh_chung", columnDefinition = "NVARCHAR(255)")
    private String evidenceImage;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.RefundStatus status;

    @Column(name = "id_nguoi_xu_li")
    private Integer processorId;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;
}
