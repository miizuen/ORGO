package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.enums.ArticleType;
import com.example.orgo_project.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class PublicArticleController {
    
    private final ArticleService articleService;
    
    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getArticles(
            @RequestParam(required = false) ArticleType type,
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<ArticleResponse> articles = articleService.getPublicArticles(category, pageable);
        return ResponseEntity.ok(articles);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Integer id) {
        ArticleResponse response = articleService.getArticleById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<ArticleResponse>> getFeaturedArticles() {
        List<ArticleResponse> articles = articleService.getFeaturedArticles();
        return ResponseEntity.ok(articles);
    }
}
