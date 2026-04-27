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
@Table(name = "LichSuVanChuyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lich_su_van_chuyen")
    private Integer id;

    @Column(name = "id_van_chuyen")
    private Integer shipmentId;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.ShipmentStatus status;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "thoi_gian")
    private LocalDateTime happenedAt;
}
