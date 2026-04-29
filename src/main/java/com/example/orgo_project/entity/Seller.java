package com.example.orgo_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "NhaBanHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nha_ban_hang")
    private Integer id;

    @Column(name = "ten_shop", columnDefinition = "NVARCHAR(255)")
    private String shopName;

    @Column(name = "dia_chi_shop", columnDefinition = "NVARCHAR(255)")
    private String shopAddress;

    @Column(name = "ma_so_thue", columnDefinition = "NVARCHAR(255)")
    private String taxCode;

    @Column(name = "file_dang_ky_kinh_doanh", columnDefinition = "NVARCHAR(255)")
    private String businessRegistrationFile;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.SellerStatus status;

    @Column(name = "ten_chu_shop", columnDefinition = "NVARCHAR(255)")
    private String ownerName;

    @Column(name = "so_dien_thoai_chu_shop", columnDefinition = "NVARCHAR(255)")
    private String ownerPhoneNumber;

    @Column(name = "email_chu_shop", columnDefinition = "NVARCHAR(255)")
    private String ownerEmail;

    @Column(name = "email_shop", columnDefinition = "NVARCHAR(255)")
    private String shopEmail;

    @Column(name = "mo_ta_shop", columnDefinition = "NVARCHAR(MAX)")
    private String shopDescription;

    @ManyToOne
    @JoinColumn(name="id_tai_khoan")
    private Account account;
}
