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

    @Column(name = "ten_chuyen_muc")
    private String categoryName;

    @Column(name = "slug")
    private String slug;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "anh_dai_dien")
    private String imageUrl;
}
