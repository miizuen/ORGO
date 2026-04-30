package com.example.orgo_project.repository;

import com.example.orgo_project.entity.Article;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.enums.ArticleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Integer> {
    
    Page<Article> findByExpertId(Integer expertId, Pageable pageable);
    
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedArticles(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.viewCount DESC")
    List<Article> findFeaturedArticles(Pageable pageable);
}
