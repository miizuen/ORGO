package com.example.orgo_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TaiKhoan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tai_khoan")
    private Integer id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "mat_khau", nullable = false)
    private String password;

    @Column(name = "ngayDangKy")
    private String registeredAt;

    @Column(name = "anhDaiDien")
    private String avatarUrl;

    @Column(name = "nhaBanTin")
    private Boolean newsletterSubscribed;

    @Column(name = "id_vai_tro")
    private Integer roleId;
}
