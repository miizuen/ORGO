package com.example.orgo_project.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.orgo_project.entity.OrganicCertificate;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductReview;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.repository.IProductCategoryRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.IUserRepository;
import com.example.orgo_project.security.CustomUserDetails;
import com.example.orgo_project.service.ProductService;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ISellerRepository sellerRepository;

    @Autowired
    private IProductCategoryRepository productCategoryRepository;

    // ==================== PUBLIC ====================

    // Trang danh sách sản phẩm (T025)
    @GetMapping("/products")
    public String showProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String sort,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;

        if ((q != null && !q.isBlank()) || categoryId != null) {
            products = productService.searchProducts(q, categoryId, sort, pageable);
        } else {
            products = productService.getActiveProducts(pageable);
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort);
        return "pages/public/product-page";
    }

    // Trang chi tiết sản phẩm (T026)
    @GetMapping("/products/{id}")
    public String showProductDetail(@PathVariable Integer id, Model model,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/products";

        List<ProductVariant> variants = productService.getVariantsByProduct(id);
        List<OrganicCertificate> certs = productService.getCertsByProduct(id);
        Page<ProductReview> reviews = productService.getReviewsByProduct(id, PageRequest.of(0, 10));

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("certs", certs);
        model.addAttribute("reviews", reviews);

        // Build map userId -> fullName cho đánh giá
        java.util.Map<Integer, String> reviewerNames = new java.util.HashMap<>();
        java.util.Map<Integer, String> sellerNames = new java.util.HashMap<>();

        // Lấy tên shop của sản phẩm này làm default (dùng khi reply chưa có sellerId)
        String productShopName = "Seller";
        com.example.orgo_project.entity.Seller productSeller = sellerRepository.findById(product.getSellerId()).orElse(null);
        if (productSeller != null && productSeller.getShopName() != null) {
            productShopName = productSeller.getShopName();
        }
        model.addAttribute("productShopName", productShopName);

        for (ProductReview review : reviews.getContent()) {
            if (review.getUserId() != null && !reviewerNames.containsKey(review.getUserId())) {
                UserProfile reviewer = userRepository.findById(review.getUserId()).orElse(null);
                reviewerNames.put(review.getUserId(),
                        reviewer != null && reviewer.getFullName() != null ? reviewer.getFullName() : "Người dùng");
            }
            if (review.getSellerId() != null && !sellerNames.containsKey(review.getSellerId())) {
                com.example.orgo_project.entity.Seller seller = sellerRepository.findById(review.getSellerId()).orElse(null);
                sellerNames.put(review.getSellerId(),
                        seller != null && seller.getShopName() != null ? seller.getShopName() : "Seller");
            }
        }
        model.addAttribute("reviewerNames", reviewerNames);
        model.addAttribute("sellerNames", sellerNames);

        if (userDetails != null) {
            // Tìm user theo username (vì login dùng username)
            UserProfile user = userRepository.findByEmail(userDetails.getUsername());
            if (user == null) {
                // Tìm theo account username
                com.example.orgo_project.entity.Account acc = userDetails.getAccount();
                if (acc != null && acc.getUser() != null) {
                    user = acc.getUser();
                }
            }
            if (user != null) {
                model.addAttribute("hasReviewed", productService.hasReviewed(id, user.getId()));
                model.addAttribute("currentUserId", user.getId());
            } else {
                // Vẫn cho phép xem form nếu đã login
                model.addAttribute("hasReviewed", false);
                model.addAttribute("currentUserId", userDetails.getAccount() != null ? userDetails.getAccount().getId() : null);
            }
        }

        return "pages/public/product-detail";
    }

    // Search suggestions (AJAX - T030)
    @GetMapping("/products/search/suggestions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSuggestions(@RequestParam String q) {
        List<Product> products = productService.getSuggestions(q);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Product p : products) {
            result.add(Map.of(
                    "id", p.getId(),
                    "name", p.getProductName()
            ));
        }
        return ResponseEntity.ok(result);
    }

    // ==================== SELLER ====================

    // Danh sách sản phẩm của seller (T027)
    @GetMapping("/seller/products")
    public String sellerProducts(@RequestParam(defaultValue = "0") int page,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model) {
        Integer sellerId = getSellerIdFromUser(userDetails);
        if (sellerId == null) return "redirect:/";

        Page<Product> products = productService.getProductsBySeller(sellerId, PageRequest.of(page, 10));
        model.addAttribute("activePage", "products");
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        return "pages/seller/products";
    }

    // Form thêm sản phẩm
    @GetMapping("/seller/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("certs", java.util.Collections.emptyList());
        model.addAttribute("action", "new");
        return "pages/seller/product-form";
    }

    // Lưu sản phẩm mới
    @PostMapping("/seller/products/new")
    public String createProduct(@ModelAttribute Product product,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 @RequestParam(required = false) List<String> variantNames,
                                 @RequestParam(required = false) List<BigDecimal> variantPrices,
                                 @RequestParam(required = false) List<Integer> variantStocks,
                                 @RequestParam(required = false) List<String> certNames,
                                 @RequestParam(required = false) List<String> certOrgs,
                                 @RequestParam(required = false) List<String> certDates,
                                 @RequestParam(required = false) List<MultipartFile> certFiles,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer sellerId = getSellerIdFromUser(userDetails);
        if (sellerId == null) return "redirect:/";

        product.setSellerId(sellerId);
        List<ProductVariant> variants = buildVariants(variantNames, variantPrices, variantStocks);
        Product saved = productService.createProduct(product, variants, imageFile);
        if (saved != null) {
            productService.saveCertificates(saved.getId(), null, certNames, certOrgs, certDates, certFiles);
        }
        return "redirect:/seller/products?success=created";
    }

    // Form sửa sản phẩm
    @GetMapping("/seller/products/{id}/edit")
    public String editProductForm(@PathVariable Integer id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/seller/products";

        Integer sellerId = getSellerIdFromUser(userDetails);
        if (!product.getSellerId().equals(sellerId)) return "redirect:/seller/products";

        model.addAttribute("product", product);
        model.addAttribute("variants", productService.getVariantsByProduct(id));
        model.addAttribute("certs", productService.getCertsByProduct(id));
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("action", "edit");
        return "pages/seller/product-form";
    }

    // Cập nhật sản phẩm
    @PostMapping("/seller/products/{id}/edit")
    public String updateProduct(@PathVariable Integer id,
                                 @ModelAttribute Product product,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 @RequestParam(required = false) List<String> variantNames,
                                 @RequestParam(required = false) List<BigDecimal> variantPrices,
                                 @RequestParam(required = false) List<Integer> variantStocks,
                                 @RequestParam(required = false) List<Integer> keepCertIds,
                                 @RequestParam(required = false) List<String> certNames,
                                 @RequestParam(required = false) List<String> certOrgs,
                                 @RequestParam(required = false) List<String> certDates,
                                 @RequestParam(required = false) List<MultipartFile> certFiles,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer sellerId = getSellerIdFromUser(userDetails);
        if (sellerId == null) return "redirect:/";

        product.setId(id);
        product.setSellerId(sellerId);
        List<ProductVariant> variants = buildVariants(variantNames, variantPrices, variantStocks);
        productService.updateProduct(product, variants, imageFile);
        productService.saveCertificates(id, keepCertIds, certNames, certOrgs, certDates, certFiles);
        return "redirect:/seller/products?success=updated";
    }

    // Xóa mềm sản phẩm
    @PostMapping("/seller/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer sellerId = getSellerIdFromUser(userDetails);
        Product product = productService.getProductById(id);
        if (product != null && product.getSellerId().equals(sellerId)) {
            productService.softDeleteProduct(id);
        }
        return "redirect:/seller/products?success=deleted";
    }

    // Reply review (T044)
    @PostMapping("/seller/reviews/{reviewId}/reply")
    public String replyReview(@PathVariable Integer reviewId,
                               @RequestParam String replyText,
                               @RequestParam Integer productId,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer sellerId = getSellerIdFromUser(userDetails);
        productService.replyReview(reviewId, replyText, sellerId);
        return "redirect:/products/" + productId + "#reviews";
    }

    // ==================== ADMIN ====================

    // Danh sách sản phẩm chờ duyệt (T028)
    @GetMapping("/admin/products")
    public String adminProducts(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "pending") String status,
                                 Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> products;
        if ("all".equals(status)) {
            products = productService.getAllProductsForAdmin(pageable);
        } else {
            products = productService.getPendingProducts(pageable);
        }
        model.addAttribute("activePage", "products");
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("status", status);
        return "pages/admin/products";
    }

    @GetMapping("/admin/products/{id}")
    public String adminProductDetail(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/admin/products";

        List<ProductVariant> variants = productService.getVariantsByProduct(id);
        List<OrganicCertificate> certs = productService.getCertsByProduct(id);
        Page<ProductReview> reviews = productService.getReviewsByProduct(id, PageRequest.of(0, 10));

        model.addAttribute("activePage", "products");
        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("certs", certs);
        model.addAttribute("reviews", reviews);
        model.addAttribute("productShopName", getShopName(product));
        model.addAttribute("productCategoryName", getCategoryName(product));
        return "pages/admin/product-detail";
    }

    // Duyệt sản phẩm (T023)
    @PostMapping("/admin/products/{id}/approve")
    public String approveProduct(@PathVariable Integer id) {
        productService.approveProduct(id);
        return "redirect:/admin/products?success=approved";
    }

    // Từ chối sản phẩm (T023)
    @PostMapping("/admin/products/{id}/reject")
    public String rejectProduct(@PathVariable Integer id) {
        productService.rejectProduct(id);
        return "redirect:/admin/products?success=rejected";
    }

    // ==================== REVIEW ====================

    // Thêm đánh giá (T044, T045)
    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Integer productId,
                             @RequestParam Integer stars,
                             @RequestParam String content,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";

        // Lấy userId từ account
        Integer userId = null;
        UserProfile user = userRepository.findByEmail(userDetails.getUsername());
        if (user != null) {
            userId = user.getId();
        } else if (userDetails.getAccount() != null && userDetails.getAccount().getUser() != null) {
            userId = userDetails.getAccount().getUser().getId();
        }

        if (userId == null) return "redirect:/products/" + productId;

        if (productService.hasReviewed(productId, userId)) {
            return "redirect:/products/" + productId + "?error=already_reviewed";
        }

        ProductReview review = new ProductReview();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setStars(stars);
        review.setContent(content);
        review.setReviewedAt(java.time.LocalDateTime.now());
        review.setStatus(com.example.orgo_project.enums.ReviewStatus.APPROVED);

        productService.addReview(review);
        return "redirect:/products/" + productId + "?success=reviewed#reviews";
    }

    // ==================== HELPER ====================

    private String getShopName(Product product) {
        if (product == null || product.getSellerId() == null) return "Seller";
        com.example.orgo_project.entity.Seller seller = sellerRepository.findById(product.getSellerId()).orElse(null);
        if (seller != null && seller.getShopName() != null && !seller.getShopName().isBlank()) {
            return seller.getShopName();
        }
        return "Seller";
    }

    private String getCategoryName(Product product) {
        if (product == null || product.getCategoryId() == null) return "Chưa phân loại";
        return productCategoryRepository.findById(product.getCategoryId())
                .map(com.example.orgo_project.entity.ProductCategory::getCategoryName)
                .filter(name -> name != null && !name.isBlank())
                .orElse("Chưa phân loại");
    }

    private Integer getSellerIdFromUser(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) return null;

        return sellerRepository.findByAccount(userDetails.getAccount())
                .map(com.example.orgo_project.entity.Seller::getId)
                .orElse(null);
    }

    private List<ProductVariant> buildVariants(List<String> names, List<BigDecimal> prices, List<Integer> stocks) {
        List<ProductVariant> variants = new ArrayList<>();
        if (names == null) return variants;
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i) == null || names.get(i).isBlank()) continue;
            ProductVariant v = new ProductVariant();
            v.setVariantName(names.get(i));
            v.setOriginalPrice(prices != null && i < prices.size() ? prices.get(i) : BigDecimal.ZERO);
            v.setStockQuantity(stocks != null && i < stocks.size() ? stocks.get(i) : 0);
            variants.add(v);
        }
        return variants;
    }
}
