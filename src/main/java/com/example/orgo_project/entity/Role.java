package com.example.orgo_project.entity;

import com.example.orgo_project.enums.RoleName;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VaiTro")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vai_tro")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ten_vai_tro", columnDefinition = "NVARCHAR(255)")
    private RoleName roleName;

    @jakarta.persistence.OneToMany(mappedBy = "role")
    private java.util.List<Account> accounts;
}
