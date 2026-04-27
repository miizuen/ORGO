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
@Table(name = "ChungNhanHuuCo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganicCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chung_nhan_huu_co")
    private Integer id;

    @Column(name = "id_san_pham")
    private Integer productId;

    @Column(name = "ten_chung_nhan")
    private String certificateName;

    @Column(name = "toChucCap")
    private String issuingOrganization;

    @Column(name = "ngay_cap")
    private LocalDateTime issuedAt;

    @Column(name = "ngay_het_han")
    private LocalDateTime expiresAt;

    @Column(name = "file_dinh_kem")
    private String attachmentFile;

    @Column(name = "trangThaiDuyet")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.CertificateStatus status;
}
