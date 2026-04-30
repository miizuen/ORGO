package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;
    private final com.example.orgo_project.repository.ProductRepository productRepository;
    private final com.example.orgo_project.repository.IProductVariantRepository productVariantRepository;

    @GetMapping
    public String getArticles(
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Page<ArticleResponse> articles = status == ArticleStatus.PENDING
                ? articleService.getPendingArticles(PageRequest.of(page, size))
                : articleService.getPublicArticles(null, PageRequest.of(page, size));

        model.addAttribute("activePage", "articles");
        model.addAttribute("articles", articles);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articles.getTotalPages());
        return "pages/admin/articles";
    }

    @GetMapping("/{id}")
    public String getArticle(@PathVariable Integer id, Model model) {
        ArticleResponse article = articleService.getArticleById(id);
        List<Product> linkedProducts = productRepository.findAllById(article.getProductIds());

        for (Product product : linkedProducts) {
            if (product.getSlug() != null && product.getSlug().startsWith("/uploads/")) {
                product.setImageUrl(product.getSlug());
            }
        }

        model.addAttribute("article", article);
        model.addAttribute("linkedProducts", linkedProducts);
        return "pages/admin/article-approval-detail";
    }

    @PostMapping("/{id}/approve")
    public String approveArticle(@PathVariable Integer id) {
        articleService.approveArticle(id);
        return "redirect:/admin/articles?success=approved";
    }

    @PostMapping("/{id}/reject")
    public String rejectArticle(@PathVariable Integer id, @RequestParam("note") String note) {
        articleService.rejectArticle(id, note);
        return "redirect:/admin/articles?success=rejected";
    }
}
