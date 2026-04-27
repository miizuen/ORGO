package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ArticleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleProductRepository extends JpaRepository<ArticleProduct, Long> {
    
    List<ArticleProduct> findByArticleId(Long articleId);
    
    void deleteByArticleId(Long articleId);
}
