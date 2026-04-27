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
@Table(name = "sanPhamTrongBaiViet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_san_pham_bai_viet")
    private Integer id;

    @Column(name = "id_bai_viet")
    private Integer articleId;

    @Column(name = "id_san_pham")
    private Integer productId;
}
