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
@Table(name = "ChuyenMuc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chuyen_muc")
    private Integer id;

    @Column(name = "ten_chuyen_muc", columnDefinition = "NVARCHAR(255)")
    private String categoryName;

    @Column(name = "slug", columnDefinition = "NVARCHAR(255)")
    private String slug;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "anh_dai_dien", columnDefinition = "NVARCHAR(255)")
    private String imageUrl;
}
