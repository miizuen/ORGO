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
@Table(name = "ChuyenGia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "id_tai_khoan")
    private Integer accountId;

    @Column(name = "linh_vuc_chuyen_mon", columnDefinition = "NVARCHAR(255)")
    private String expertiseField;

    @Column(name = "file_chung_chi", columnDefinition = "NVARCHAR(255)")
    private String certificateFile;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ExpertStatus status;

    @Column(name = "mo_ta_kinh_nghiem", columnDefinition = "NVARCHAR(MAX)")
    private String experienceDescription;

    @Column(name = "ngay_tao")
    private LocalDateTime createdAt;
}
