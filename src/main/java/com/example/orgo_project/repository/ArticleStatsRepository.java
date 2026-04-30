package com.example.orgo_project.repository;

import com.example.orgo_project.entity.ArticleStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleStatsRepository extends JpaRepository<ArticleStats, Long> {
    
    Optional<ArticleStats> findByArticleIdAndDate(Long articleId, LocalDate date);
    
    List<ArticleStats> findByArticleIdOrderByDateDesc(Long articleId);
    
    @Query("SELECT SUM(s.views) FROM ArticleStats s WHERE s.article.id = :articleId")
    Integer getTotalViewsByArticleId(Long articleId);
    
    @Query("SELECT SUM(s.likes) FROM ArticleStats s WHERE s.article.id = :articleId")
    Integer getTotalLikesByArticleId(Long articleId);
}
