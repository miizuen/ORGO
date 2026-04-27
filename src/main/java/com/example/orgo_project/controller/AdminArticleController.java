package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ApprovalRequest;
import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {
    
    private final ArticleService articleService;
    
    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getArticles(
            @RequestParam(required = false) ArticleStatus status,
            Pageable pageable) {
        Page<ArticleResponse> articles;
        if (status == ArticleStatus.PENDING) {
            articles = articleService.getPendingArticles(pageable);
        } else {
            articles = articleService.getPublicArticles(null, null, pageable);
        }
        return ResponseEntity.ok(articles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id) {
        ArticleResponse response = articleService.getArticleById(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/approve")
    public ResponseEntity<ArticleResponse> approveArticle(@PathVariable Long id) {
        ArticleResponse response = articleService.approveArticle(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/reject")
    public ResponseEntity<ArticleResponse> rejectArticle(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request) {
        ArticleResponse response = articleService.rejectArticle(id, request.getNote());
        return ResponseEntity.ok(response);
    }
}
