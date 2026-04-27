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
@Table(name = "DanhGiaSanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bai_danh_gia")
    private Integer id;

    @Column(name = "id_san_pham")
    private Integer productId;

    @Column(name = "id_nguoi_dung")
    private Integer userId;

    @Column(name = "so_sao")
    private Integer stars;

    @Column(name = "noi_dung")
    private String content;

    @Column(name = "hinh_anh")
    private String imageUrl;

    @Column(name = "ngay_danh_gia")
    private LocalDateTime reviewedAt;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ReviewStatus status;

    @Column(name = "huuIch")
    private Integer helpfulCount;

    @Column(name = "phan_hoi")
    private String reply;

    @Column(name = "ngay_phan_hoi")
    private LocalDateTime repliedAt;

    @Column(name = "id_nha_ban_hang")
    private Integer sellerId;
}
