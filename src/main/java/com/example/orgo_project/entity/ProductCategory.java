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
@Table(name = "DanhMucSanPham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tenDanhMuc", columnDefinition = "NVARCHAR(255)")
    private String categoryName;

    @Column(name = "slug", columnDefinition = "NVARCHAR(255)")
    private String slug;

    @Column(name = "moTa", columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "anhDaiDien", columnDefinition = "NVARCHAR(255)")
    private String imageUrl;
}
