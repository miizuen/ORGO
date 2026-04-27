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
@Table(name = "VanChuyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_van_chuyen")
    private Integer id;

    @Column(name = "id_don_hang")
    private Integer orderId;

    @Column(name = "don_vi_van_chuyen")
    private String shippingProvider;

    @Column(name = "ma_van_don")
    private String trackingCode;

    @Column(name = "ngay_giao_du_kien")
    private LocalDateTime estimatedDeliveryAt;

    @Column(name = "ngay_giao_thuc_te")
    private LocalDateTime actualDeliveryAt;
}
