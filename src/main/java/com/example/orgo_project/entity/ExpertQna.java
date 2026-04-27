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
@Table(name = "HoiDapChuyenMon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpertQna {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "khachThanhVienid")
    private Integer memberAccountId;

    @Column(name = "chuyenGiaId")
    private Integer expertId;

    @Column(name = "cauHoi", columnDefinition = "NVARCHAR(MAX)")
    private String question;

    @Column(name = "cauTraLoi", columnDefinition = "NVARCHAR(MAX)")
    private String answer;

    @Column(name = "ngayHoi")
    private LocalDateTime askedAt;

    @Column(name = "ngayTraLoi")
    private LocalDateTime answeredAt;

    @Column(name = "trangThai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.QnaStatus status;
}
