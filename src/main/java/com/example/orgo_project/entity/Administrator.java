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
@Table(name = "QuanTriVien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", columnDefinition = "NVARCHAR(255)")
    private String username;

    @Column(name = "matKhau", columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "hoTen", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "vaiTro", columnDefinition = "NVARCHAR(255)")
    private String role;

    @Column(name = "trangThai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.UserStatus status;
}
