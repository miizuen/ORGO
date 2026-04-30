package com.example.orgo_project.service;

import com.example.orgo_project.dto.*;
import com.example.orgo_project.entity.*;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    
    private final ArticleRepository articleRepository;
    private final ArticleProductRepository articleProductRepository;
    private final ArticleStatsRepository articleStatsRepository;
    
    @Transactional
    public ArticleResponse createArticle(Integer expertId, ArticleRequest request) {
        Article article = new Article();
        article.setExpertId(expertId);
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getThumbnail());
        article.setSummary(request.getCategory());
        article.setStatus(ArticleStatus.DRAFT);
        article.setPublishedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        article.setViewCount(0);
        
        Article saved = articleRepository.save(article);
        
        // Link products if provided
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            linkProducts(saved.getId(), request.getProductIds());
        }
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public ArticleResponse updateArticle(Integer articleId, ArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        // Only allow update if DRAFT or REJECTED
        if (article.getStatus() != ArticleStatus.DRAFT && article.getStatus() != ArticleStatus.REJECTED) {
            throw new RuntimeException("Can only update DRAFT or REJECTED articles");
        }
        
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getThumbnail());
        article.setSummary(request.getCategory());
        article.setUpdatedAt(LocalDateTime.now());
        
        Article updated = articleRepository.save(article);
        
        // Update product links
        articleProductRepository.deleteByArticleId(articleId.longValue());
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            linkProducts(updated.getId(), request.getProductIds());
        }
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public void deleteArticle(Integer articleId) {
        articleRepository.deleteById(articleId);
    }
    
    @Transactional
    public ArticleResponse submitArticle(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.PENDING);
        Article updated = articleRepository.save(article);
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public void linkProducts(Integer articleId, List<Long> productIds) {
        for (Long productId : productIds) {
            ArticleProduct ap = new ArticleProduct();
            ap.setArticleId(articleId);
            ap.setProductId(productId.intValue());
            articleProductRepository.save(ap);
        }
    }
    
    public Page<ArticleResponse> getExpertArticles(Integer expertId, Pageable pageable) {
        return articleRepository.findByExpertId(expertId, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<ArticleResponse> getPendingArticles(Pageable pageable) {
        return articleRepository.findByStatus(ArticleStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public ArticleResponse approveArticle(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        Article updated = articleRepository.save(article);
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public ArticleResponse rejectArticle(Integer articleId, String note) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.REJECTED);
        article.setRejectionReason(note);
        Article updated = articleRepository.save(article);
        
        return mapToResponse(updated);
    }
    
    public Page<ArticleResponse> getPublicArticles(String category, Pageable pageable) {
        return articleRepository.findPublishedArticles(pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public ArticleResponse getArticleById(Integer articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        
        // Update stats
        LocalDate today = LocalDate.now();
        ArticleStats stats = articleStatsRepository.findByArticleIdAndDate(articleId.longValue(), today)
                .orElse(new ArticleStats(null, article, 0, 0, today));
        stats.setViews(stats.getViews() + 1);
        articleStatsRepository.save(stats);
        
        return mapToResponse(article);
    }
    
    public List<ArticleStatsResponse> getArticleStats(Integer articleId) {
        return articleStatsRepository.findByArticleIdOrderByDateDesc(articleId.longValue()).stream()
                .map(s -> new ArticleStatsResponse(s.getDate(), s.getViews(), s.getLikes()))
                .collect(Collectors.toList());
    }
    
    public List<ArticleResponse> getFeaturedArticles() {
        return articleRepository.findFeaturedArticles(PageRequest.of(0, 5)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    private ArticleResponse mapToResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId().longValue());
        response.setExpertId(article.getExpertId().longValue());
        response.setExpertName("Expert " + article.getExpertId()); // Simplified
        response.setTitle(article.getTitle());
        response.setContent(article.getContent());
        response.setStatus(article.getStatus());
        response.setThumbnail(article.getCoverImage());
        response.setCategory(article.getSummary());
        response.setRejectNote(article.getRejectionReason());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getUpdatedAt());
        response.setTotalViews(article.getViewCount());
        response.setTotalLikes(0);
        
        // Get linked products
        List<Long> productIds = articleProductRepository.findByArticleId(article.getId().longValue()).stream()
                .map(ap -> ap.getProductId().longValue())
                .collect(Collectors.toList());
        response.setProductIds(productIds);
        
        return response;
    }
}
