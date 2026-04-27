package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleRequest;
import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.dto.ArticleStatsResponse;
import com.example.orgo_project.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expert/articles")
@RequiredArgsConstructor
public class ExpertArticleController {
    
    private final ArticleService articleService;
    
    @PostMapping
    public ResponseEntity<ArticleResponse> createArticle(
            @Valid @RequestBody ArticleRequest request,
            Authentication authentication) {
        Long expertId = getExpertIdFromAuth(authentication);
        ArticleResponse response = articleService.createArticle(expertId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getMyArticles(
            Pageable pageable,
            Authentication authentication) {
        Long expertId = getExpertIdFromAuth(authentication);
        Page<ArticleResponse> articles = articleService.getExpertArticles(expertId, pageable);
        return ResponseEntity.ok(articles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id) {
        ArticleResponse response = articleService.getArticleById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = articleService.updateArticle(id, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<ArticleResponse> submitArticle(@PathVariable Long id) {
        ArticleResponse response = articleService.submitArticle(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{id}/link-product")
    public ResponseEntity<Void> linkProducts(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> request) {
        articleService.linkProducts(id, request.get("productIds"));
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{id}/stats")
    public ResponseEntity<List<ArticleStatsResponse>> getArticleStats(@PathVariable Long id) {
        List<ArticleStatsResponse> stats = articleService.getArticleStats(id);
        return ResponseEntity.ok(stats);
    }
    
    private Long getExpertIdFromAuth(Authentication authentication) {
        // TODO: Extract expert ID from authentication
        return 1L; // Placeholder
    }
}
