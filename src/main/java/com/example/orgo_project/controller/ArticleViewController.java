package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.enums.ArticleType;
import com.example.orgo_project.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleViewController {
    
    private final ArticleService articleService;
    
    @GetMapping
    public String listArticles(
            @RequestParam(required = false) ArticleType type,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        
        Page<ArticleResponse> articles = articleService.getPublicArticles(
                category, PageRequest.of(page, 12));
        
        List<ArticleResponse> featured = articleService.getFeaturedArticles();
        
        model.addAttribute("articles", articles);
        model.addAttribute("featured", featured);
        model.addAttribute("currentType", type);
        model.addAttribute("currentCategory", category);
        
        return "pages/public/articles-list";
    }
    
    @GetMapping("/{id}")
    public String articleDetail(@PathVariable Integer id, Model model) {
        ArticleResponse article = articleService.getArticleById(id);
        List<ArticleResponse> related = articleService.getPublicArticles(
                null, PageRequest.of(0, 3)).getContent();
        
        model.addAttribute("article", article);
        model.addAttribute("related", related);
        
        return "pages/public/article-detail";
    }
}
