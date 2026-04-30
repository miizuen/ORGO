package com.example.orgo_project.controller;

import com.example.orgo_project.dto.ArticleRequest;
import com.example.orgo_project.dto.ArticleResponse;
import com.example.orgo_project.dto.ArticleStatsResponse;
import com.example.orgo_project.entity.Article;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.enums.ArticleStatus;
import com.example.orgo_project.repository.ProductRepository;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/expert/articles")
@RequiredArgsConstructor
public class ExpertArticleController {

    private final ArticleService articleService;
    private final ProductRepository productRepository;
    private final com.example.orgo_project.repository.IProductVariantRepository productVariantRepository;

    @GetMapping
    public String getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        Integer expertId = getExpertIdFromSession();
        Page<ArticleResponse> articles = articleService.getExpertArticles(expertId, PageRequest.of(page, size));

        model.addAttribute("articles", articles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articles.getTotalPages());
        return "pages/expert/articles";
    }

    @GetMapping("/new")
    public String createArticleForm(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("article", new ArticleRequest());
        model.addAttribute("products", products);
        model.addAttribute("categories", List.of("Dinh dưỡng", "Canh tác", "Sức khỏe", "Môi trường", "Tin tức"));
        return "pages/expert/article-form";
    }

    @PostMapping("/new")
    public String createArticle(@Valid @ModelAttribute("article") ArticleRequest request,
                                BindingResult bindingResult,
                                @RequestParam(value = "coverImageFile", required = false) MultipartFile coverImageFile,
                                Model model) {

        if (request.getType() == null) {
            request.setType(com.example.orgo_project.enums.ArticleType.BLOG);
        }
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(e -> System.out.println("VALIDATION ERROR: " + e.getDefaultMessage()));
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("categories", List.of("Dinh dưỡng", "Canh tác", "Sức khỏe", "Môi trường", "Tin tức"));
            return "pages/expert/article-form";
        }

        if (coverImageFile != null && !coverImageFile.isEmpty()) {
            try {
                String originalName = coverImageFile.getOriginalFilename();
                String safeName = (originalName == null || originalName.isBlank()) ? "cover.jpg" : originalName;
                String fileName = UUID.randomUUID() + "_" + safeName;
                Path uploadDir = Paths.get("src/main/resources/static/uploads");
                Files.createDirectories(uploadDir);
                Path targetPath = uploadDir.resolve(fileName);
                Files.copy(coverImageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                request.setThumbnail("/uploads/" + fileName);
            } catch (IOException e) {
                model.addAttribute("products", productRepository.findAll());
                model.addAttribute("categories", List.of("Dinh dưỡng", "Canh tác", "Sức khỏe", "Môi trường", "Tin tức"));
                model.addAttribute("uploadError", "Không thể tải ảnh bìa lên. Vui lòng thử lại.");
                return "pages/expert/article-form";
            }
        }

        Article article = new Article();
        article.setExpertId(getExpertIdFromSession());
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCoverImage(request.getThumbnail());
        article.setSummary(request.getSummary());
        article.setStatus(ArticleStatus.PENDING);
        article.setUpdatedAt(LocalDateTime.now());
        article.setViewCount(0);
        article.setSlug(request.getTitle() != null
                ? request.getTitle().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-")
                : null);

        Article savedArticle = articleService.createArticle(article);
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            articleService.linkProducts(savedArticle.getId(), request.getProductIds());
        }
        return "redirect:/expert/articles?success=created";
    }

    @GetMapping("/{id}")
    public String getArticle(@PathVariable Integer id, Model model) {
        ArticleResponse article = articleService.getArticleById(id);
        List<ArticleResponse> related = articleService.getPublicArticles(null, PageRequest.of(0, 3)).getContent();
        List<Product> relatedProducts = productRepository.findAllById(article.getProductIds());
        for (Product product : relatedProducts) {
            java.util.List<com.example.orgo_project.entity.ProductVariant> variants = productVariantRepository.findByProductId(product.getId());
            if (!variants.isEmpty()) {
                product.setImageUrl(variants.get(0).getImageUrl());
            }
        }
        model.addAttribute("article", article);
        model.addAttribute("related", related);
        model.addAttribute("relatedProducts", relatedProducts);
        return "pages/expert/blog-detail";
    }

    @GetMapping("/{id}/stats")
    public String getArticleStats(@PathVariable Integer id, Model model) {
        List<ArticleStatsResponse> stats = articleService.getArticleStats(id);
        model.addAttribute("stats", stats);
        return "pages/expert/article-stats";
    }

    @PostMapping("/{id}/submit")
    public String submitArticle(@PathVariable Integer id) {
        articleService.submitArticle(id);
        return "redirect:/expert/articles?success=submitted";
    }

    @PostMapping("/{id}/delete")
    public String deleteArticle(@PathVariable Integer id) {
        articleService.deleteArticle(id);
        return "redirect:/expert/articles?success=deleted";
    }

    private Integer getExpertIdFromSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getAccount().getId();
        }
        return null;
    }
}
