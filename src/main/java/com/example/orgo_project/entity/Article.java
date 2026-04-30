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
@Table(name = "BaiViet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bai_viet")
    private Integer id;

    @Column(name = "id_chuyen_gia")
    private Integer expertId;

    @Column(name = "id_nguoi_duyet")
    private Integer reviewerAdminId;

    @Column(name = "id_chuyen_muc")
    private Integer blogCategoryId;

    @Column(name = "tieu_de", columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column(name = "slug", columnDefinition = "NVARCHAR(255)")
    private String slug;

    @Column(name = "anh_bia", columnDefinition = "NVARCHAR(255)")
    private String coverImage;

    @Column(name = "tom_tat", columnDefinition = "NVARCHAR(MAX)")
    private String summary;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "ngay_dang")
    private LocalDateTime publishedAt;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime updatedAt;

    @Column(name = "luot_xem")
    private Integer viewCount;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ArticleStatus status;

    @Column(name = "ly_do_tu_choi", columnDefinition = "NVARCHAR(255)")
    private String rejectionReason;
}
