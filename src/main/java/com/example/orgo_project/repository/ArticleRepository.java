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

    Page<Article> findByExpertIdAndStatus(Integer expertId, ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.expertId = :expertId " +
           "AND (:search IS NULL OR :search = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> searchByExpertId(
            @org.springframework.data.repository.query.Param("expertId") Integer expertId,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.expertId = :expertId " +
           "AND a.status = :status " +
           "AND (:search IS NULL OR :search = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> searchByExpertIdAndStatus(
            @org.springframework.data.repository.query.Param("expertId") Integer expertId,
            @org.springframework.data.repository.query.Param("status") ArticleStatus status,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);
    
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedArticles(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' ORDER BY a.viewCount DESC")
    List<Article> findFeaturedArticles(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.status = 'PUBLISHED' AND LOWER(a.category) = LOWER(:category)")
    long countByCategory(@org.springframework.data.repository.query.Param("category") String category);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.status = 'PUBLISHED'")
    long countAllPublished();

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' " +
           "AND (:category IS NULL OR :category = '' OR LOWER(a.category) = LOWER(:category)) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.summary) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Article> findPublishedArticlesWithFilters(
            @org.springframework.data.repository.query.Param("category") String category,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);
}
