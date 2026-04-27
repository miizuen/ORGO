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
@Table(name = "NguoiDung")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nguoi_dung")
    private Integer id;

    @Column(name = "ho_ten", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "so_dien_thoai", columnDefinition = "NVARCHAR(255)")
    private String phoneNumber;

    @Column(name = "email", columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "trang_thai")
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    private com.example.orgo_project.enums.UserStatus status;

    @jakarta.persistence.OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "id_tai_khoan")
    private Account account;
}
