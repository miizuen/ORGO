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
@Table(name = "NhatKyAudit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nhat_ky")
    private Integer id;

    @Column(name = "loai_thuc_the", columnDefinition = "NVARCHAR(100)")
    private String entityType;

    @Column(name = "id_thuc_the")
    private Integer entityId;

    @Column(name = "hanh_dong", columnDefinition = "NVARCHAR(100)")
    private String action;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "id_nguoi_thuc_hien")
    private Integer actorId;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;
}
