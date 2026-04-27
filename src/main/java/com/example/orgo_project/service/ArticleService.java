package com.example.orgo_project.service;

import com.example.orgo_project.dto.*;
import com.example.orgo_project.entity.*;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.enums.ArticleType;
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
    private final IExpertRepository expertRepository;
    
    @Transactional
    public ArticleResponse createArticle(Long expertId, ArticleRequest request) {
        Expert expert = expertRepository.findById(expertId)
                .orElseThrow(() -> new RuntimeException("Expert not found"));
        
        Article article = new Article();
        article.setExpert(expert);
        article.setTitle(request.getTitle());
        article.setType(request.getType());
        article.setContent(request.getContent());
        article.setThumbnail(request.getThumbnail());
        article.setCategory(request.getCategory());
        article.setStatus(ArticleStatus.DRAFT);
        article.setCreatedAt(LocalDateTime.now());
        
        Article saved = articleRepository.save(article);
        
        // Link products if provided
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            linkProducts(saved.getId(), request.getProductIds());
        }
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public ArticleResponse updateArticle(Long articleId, ArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        // Only allow update if DRAFT or REJECTED
        if (article.getStatus() != ArticleStatus.DRAFT && article.getStatus() != ArticleStatus.REJECTED) {
            throw new RuntimeException("Can only update DRAFT or REJECTED articles");
        }
        
        article.setTitle(request.getTitle());
        article.setType(request.getType());
        article.setContent(request.getContent());
        article.setThumbnail(request.getThumbnail());
        article.setCategory(request.getCategory());
        
        Article updated = articleRepository.save(article);
        
        // Update product links
        articleProductRepository.deleteByArticleId(articleId);
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            linkProducts(articleId, request.getProductIds());
        }
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public void deleteArticle(Long articleId) {
        articleRepository.deleteById(articleId);
    }
    
    @Transactional
    public ArticleResponse submitArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.PENDING);
        Article updated = articleRepository.save(article);
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public void linkProducts(Long articleId, List<Long> productIds) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        for (Long productId : productIds) {
            ArticleProduct ap = new ArticleProduct();
            ap.setArticle(article);
            ap.setProductId(productId);
            articleProductRepository.save(ap);
        }
    }
    
    public Page<ArticleResponse> getExpertArticles(Long expertId, Pageable pageable) {
        return articleRepository.findByExpertId(expertId, pageable)
                .map(this::mapToResponse);
    }
    
    public Page<ArticleResponse> getPendingArticles(Pageable pageable) {
        return articleRepository.findByStatus(ArticleStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public ArticleResponse approveArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.APPROVED);
        article.setPublishedAt(LocalDateTime.now());
        Article updated = articleRepository.save(article);
        
        // TODO: Send notification to expert
        
        return mapToResponse(updated);
    }
    
    @Transactional
    public ArticleResponse rejectArticle(Long articleId, String note) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        article.setStatus(ArticleStatus.REJECTED);
        article.setRejectNote(note);
        Article updated = articleRepository.save(article);
        
        // TODO: Send notification to expert
        
        return mapToResponse(updated);
    }
    
    public Page<ArticleResponse> getPublicArticles(ArticleType type, String category, Pageable pageable) {
        if (type != null) {
            return articleRepository.findByStatusAndType(ArticleStatus.APPROVED, type, pageable)
                    .map(this::mapToResponse);
        }
        return articleRepository.findPublishedArticles(pageable)
                .map(this::mapToResponse);
    }
    
    @Transactional
    public ArticleResponse getArticleById(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        
        // Increment view count
        LocalDate today = LocalDate.now();
        ArticleStats stats = articleStatsRepository.findByArticleIdAndDate(articleId, today)
                .orElse(new ArticleStats(null, article, 0, 0, today));
        stats.setViews(stats.getViews() + 1);
        articleStatsRepository.save(stats);
        
        return mapToResponse(article);
    }
    
    public List<ArticleStatsResponse> getArticleStats(Long articleId) {
        return articleStatsRepository.findByArticleIdOrderByDateDesc(articleId).stream()
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
        response.setId(article.getId());
        response.setExpertId(article.getExpert().getId());
        response.setExpertName(article.getExpert().getUser().getFullName());
        response.setTitle(article.getTitle());
        response.setType(article.getType());
        response.setContent(article.getContent());
        response.setStatus(article.getStatus());
        response.setThumbnail(article.getThumbnail());
        response.setCategory(article.getCategory());
        response.setRejectNote(article.getRejectNote());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getCreatedAt());
        
        // Get stats
        Integer totalViews = articleStatsRepository.getTotalViewsByArticleId(article.getId());
        Integer totalLikes = articleStatsRepository.getTotalLikesByArticleId(article.getId());
        response.setTotalViews(totalViews != null ? totalViews : 0);
        response.setTotalLikes(totalLikes != null ? totalLikes : 0);
        
        // Get linked products
        List<Long> productIds = articleProductRepository.findByArticleId(article.getId()).stream()
                .map(ArticleProduct::getProductId)
                .collect(Collectors.toList());
        response.setProductIds(productIds);
        
        return response;
    }
}
