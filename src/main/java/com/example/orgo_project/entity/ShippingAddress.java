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

@Entity
@Table(name = "DiaChiGiaoHang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dia_chi")
    private Integer id;

    @Column(name = "id_tai_khoan")
    private Integer accountId;

    @Column(name = "nguoi_nhan")
    private String recipientName;

    @Column(name = "so_dien_thoai_nguoi_nhan")
    private String recipientPhone;

    @Column(name = "tinh_thanh")
    private String provinceOrCity;

    @Column(name = "co_mac_dinh")
    private Boolean defaultAddress;

    @Column(name = "loai_dia_chi")
    private String addressType;

    @Column(name = "dia_chi_cu_the")
    private String detailedAddress;
}
