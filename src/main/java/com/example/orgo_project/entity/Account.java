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

    @Column(name = "username", unique = true, nullable = false, columnDefinition = "NVARCHAR(255)")
    private String username;

    @Column(name = "mat_khau", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @Column(name = "anhDaiDien", columnDefinition = "NVARCHAR(255)")
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vai_tro")
    private Role role;

    @OneToOne(mappedBy = "account")
    private UserProfile user;
}
