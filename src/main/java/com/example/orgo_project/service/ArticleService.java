package com.example.orgo_project.service;

import com.example.orgo_project.dto.*;
import com.example.orgo_project.entity.*;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleProductRepository articleProductRepository;
    @Autowired
    private ArticleStatsRepository articleStatsRepository;
    @Autowired
    private com.example.orgo_project.repository.IExpertRepository expertRepository;
    

    public Article createArticle(Article article) {
        return articleRepository.save(article);
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
        article.setSummary(request.getSummary());
        article.setUpdatedAt(LocalDateTime.now());
        
        Article updated = articleRepository.save(article);
        
        // Update product links
        articleProductRepository.deleteByArticleId(articleId);
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
    public void linkProducts(Integer articleId, List<Integer> productIds) {
        for (Integer productId : productIds) {
            ArticleProduct ap = new ArticleProduct();
            ap.setArticleId(articleId);
            ap.setProductId(productId);
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
        Page<Article> articles = articleRepository.findPublishedArticles(pageable);
        if (category != null && !category.isBlank()) {
            List<Article> filtered = articles.getContent().stream()
                    .filter(article -> category.equalsIgnoreCase(article.getSummary()))
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered.stream().map(this::mapToResponse).collect(Collectors.toList()), pageable, filtered.size());
        }
        return articles.map(this::mapToResponse);
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
    
    private String generateSlug(String title) {
        if (title == null) {
            return null;
        }
        return title.trim().toLowerCase()
                .replaceAll("[^a-z0-9\u00C0-\u024F]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private ArticleResponse mapToResponse(Article article) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setExpertId(article.getExpertId());
        
        com.example.orgo_project.entity.Expert expert = expertRepository.findById(article.getExpertId()).orElse(null);
        if (expert != null && expert.getAccount() != null) {
            String expertName = expert.getAccount().getUsername();
            if (expert.getAccount().getUser() != null && expert.getAccount().getUser().getFullName() != null) {
                expertName = expert.getAccount().getUser().getFullName();
            }
            response.setExpertName(expertName);
            
            String defaultAvatar = "https://ui-avatars.com/api/?name=" + java.net.URLEncoder.encode(expertName, java.nio.charset.StandardCharsets.UTF_8) + "&background=random";
            response.setExpertAvatar(expert.getAccount().getAvatarUrl() != null && !expert.getAccount().getAvatarUrl().isEmpty() ? expert.getAccount().getAvatarUrl() : defaultAvatar);
        } else {
            response.setExpertName("Expert " + article.getExpertId());
            String defaultAvatar = "https://ui-avatars.com/api/?name=Expert+" + article.getExpertId() + "&background=random";
            response.setExpertAvatar(defaultAvatar);
        }
        
        response.setTitle(article.getTitle());
        response.setReadTime("5 phút đọc");
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
        List<Integer> productIds = articleProductRepository.findByArticleId(article.getId()).stream()
                .map(ap -> ap.getProductId())
                .collect(Collectors.toList());
        response.setProductIds(productIds);
        
        return response;
    }
}
