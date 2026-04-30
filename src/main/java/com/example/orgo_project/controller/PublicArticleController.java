package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.enums.ArticleType;
import com.example.orgo_project.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/articles")
@RequiredArgsConstructor
public class PublicArticleController {

    private final ArticleService articleService;
    private final com.example.orgo_project.repository.ProductRepository productRepository;
    private final com.example.orgo_project.repository.IProductVariantRepository productVariantRepository;

    @GetMapping
    public String getArticles(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Page<ArticleResponse> articles = articleService.getPublicArticles(category, PageRequest.of(page, size));
        System.out.println("Total articles: " + articles.getTotalElements());
        System.out.println("Content size: " + articles.getContent().size());

        ArticleResponse featuredArticle = articles.hasContent() ? articles.getContent().get(0) : null;

        model.addAttribute("articles", articles);
        model.addAttribute("featuredArticle", featuredArticle);
        model.addAttribute("category", category);
        model.addAttribute("categories", List.of("Dinh dưỡng", "Canh tác", "Sức khỏe", "Môi trường", "Tin tức"));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articles.getTotalPages());

        return "pages/public/blog-list";
    }

    @GetMapping("/{id}")
    public String getArticle(@PathVariable Integer id, Model model) {
        ArticleResponse article = articleService.getArticleById(id);
        List<ArticleResponse> related = articleService.getPublicArticles(null, PageRequest.of(0, 3)).getContent();
        List<com.example.orgo_project.entity.Product> relatedProducts = productRepository.findAllById(article.getProductIds());
        for (com.example.orgo_project.entity.Product product : relatedProducts) {
            java.util.List<com.example.orgo_project.entity.ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            if (!variants.isEmpty()) {
                product.setImageUrl(variants.get(0).getImageUrl());
            }
        }

        model.addAttribute("article", article);
        model.addAttribute("related", related);
        model.addAttribute("relatedProducts", relatedProducts);
        return "pages/public/blog-detail";
    }

    @GetMapping("/featured")
    public String getFeaturedArticles(Model model) {
        model.addAttribute("featuredArticles", articleService.getFeaturedArticles());
        return "pages/public/blog-list";
    }
}
