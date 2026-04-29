package com.example.orgo_project.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.orgo_project.entity.OrganicCertificate;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductCategory;
import com.example.orgo_project.entity.ProductReview;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.enums.ProductStatus;
import com.example.orgo_project.repository.IOrganicCertificateRepository;
import com.example.orgo_project.repository.IProductCategoryRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductReviewRepository;
import com.example.orgo_project.repository.IProductVariantRepository;

@Service
public class ProductService {

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private IProductVariantRepository variantRepository;

    @Autowired
    private IProductCategoryRepository categoryRepository;

    @Autowired
    private IProductReviewRepository reviewRepository;

    @Autowired
    private IOrganicCertificateRepository certRepository;

    // ==================== PUBLIC ====================

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
    }

    public Page<Product> searchProducts(String keyword, Integer categoryId, String sort, Pageable pageable) {
        Sort sortObj = switch (sort == null ? "" : sort) {
            case "price_asc" -> Sort.by("id").ascending();
            case "price_desc" -> Sort.by("id").descending();
            case "rating" -> Sort.by("averageRating").descending();
            default -> Sort.by("id").descending();
        };
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        return productRepository.searchProducts(
                (keyword != null && !keyword.isBlank()) ? keyword : null,
                categoryId,
                sortedPageable
        );
    }

    public List<Product> getSuggestions(String keyword) {
        Pageable top5 = PageRequest.of(0, 5);
        return productRepository.findTop5ByKeyword(keyword, top5);
    }

    public List<Product> getFeaturedProducts() {
        return productRepository.findTop8ByStatusOrderByAverageRatingDesc(ProductStatus.ACTIVE);
    }

    public Product getProductById(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    public Page<Product> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatus(categoryId, ProductStatus.ACTIVE, pageable);
    }

    public List<ProductVariant> getVariantsByProduct(Integer productId) {
        return variantRepository.findByProductId(productId);
    }

    public List<OrganicCertificate> getCertsByProduct(Integer productId) {
        return certRepository.findByProductId(productId);
    }

    public Page<ProductReview> getReviewsByProduct(Integer productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }

    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    // ==================== SELLER ====================

    public Page<Product> getProductsBySeller(Integer sellerId, Pageable pageable) {
        return productRepository.findBySellerId(sellerId, pageable);
    }

    @Transactional
    public Product createProduct(Product product, List<ProductVariant> variants, MultipartFile imageFile) {
        product.setStatus(ProductStatus.PENDING);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            // Store image URL in slug field temporarily (or add imageUrl field)
            product.setSlug(imageUrl);
        }
        Product saved = productRepository.save(product);
        if (variants != null) {
            for (ProductVariant v : variants) {
                v.setProductId(saved.getId());
                variantRepository.save(v);
            }
        }
        return saved;
    }

    @Transactional
    public Product updateProduct(Product product, List<ProductVariant> variants, MultipartFile imageFile) {
        Product existing = productRepository.findById(product.getId()).orElse(null);
        if (existing == null) return null;
        // Only allow edit if PENDING or REJECTED
        if (existing.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.ACTIVE);
        }
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setSlug(imageUrl);
        } else {
            product.setSlug(existing.getSlug());
        }
        Product saved = productRepository.save(product);
        if (variants != null) {
            variantRepository.deleteByProductId(saved.getId());
            for (ProductVariant v : variants) {
                v.setProductId(saved.getId());
                variantRepository.save(v);
            }
        }
        return saved;
    }

    @Transactional
    public void softDeleteProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setStatus(ProductStatus.INACTIVE);
            productRepository.save(p);
        }
    }

    // ==================== ADMIN ====================

    public Page<Product> getPendingProducts(Pageable pageable) {
        return productRepository.findByStatus(ProductStatus.PENDING, pageable);
    }

    public Page<Product> getAllProductsForAdmin(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public void approveProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setStatus(ProductStatus.ACTIVE);
            productRepository.save(p);
        }
    }

    @Transactional
    public void rejectProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setStatus(ProductStatus.REJECTED);
            productRepository.save(p);
        }
    }

    // ==================== REVIEW ====================

    @Transactional
    public ProductReview addReview(ProductReview review) {
        ProductReview saved = reviewRepository.save(review);
        // Update average rating on product
        Double avg = reviewRepository.findAverageRatingByProductId(review.getProductId());
        long count = reviewRepository.countByProductId(review.getProductId());
        Product p = productRepository.findById(review.getProductId()).orElse(null);
        if (p != null) {
            p.setAverageRating(avg != null ? avg.floatValue() : 0f);
            p.setTotalReviews((int) count);
            productRepository.save(p);
        }
        return saved;
    }

    @Transactional
    public void replyReview(Integer reviewId, String replyText, Integer sellerId) {
        ProductReview r = reviewRepository.findById(reviewId).orElse(null);
        if (r != null) {
            r.setReply(replyText);
            r.setRepliedAt(java.time.LocalDateTime.now());
            if (sellerId != null) r.setSellerId(sellerId);
            reviewRepository.save(r);
        }
    }

    public boolean hasReviewed(Integer productId, Integer userId) {
        return reviewRepository.existsByProductIdAndUserId(productId, userId);
    }

    // ==================== CERTIFICATE ====================

    @Transactional
    public void saveCertificates(Integer productId, List<Integer> keepCertIds,
                                  List<String> certNames, List<String> certOrgs,
                                  List<String> certDates, List<MultipartFile> certFiles) {
        // Xóa các cert cũ không còn trong form (bị bấm ✕)
        List<OrganicCertificate> existing = certRepository.findByProductId(productId);
        for (OrganicCertificate old : existing) {
            if (keepCertIds == null || !keepCertIds.contains(old.getId())) {
                certRepository.delete(old);
            }
        }

        // Lưu các cert mới (không có id)
        if (certNames == null || certNames.isEmpty()) return;
        for (int i = 0; i < certNames.size(); i++) {
            String name = certNames.get(i);
            if (name == null || name.isBlank()) continue;

            OrganicCertificate cert = new OrganicCertificate();
            cert.setProductId(productId);
            cert.setCertificateName(name);
            cert.setIssuingOrganization(certOrgs != null && i < certOrgs.size() ? certOrgs.get(i) : "");
            if (certDates != null && i < certDates.size() && certDates.get(i) != null && !certDates.get(i).isBlank()) {
                try {
                    cert.setIssuedAt(java.time.LocalDate.parse(certDates.get(i)).atStartOfDay());
                } catch (Exception ignored) {
                    cert.setIssuedAt(java.time.LocalDateTime.now());
                }
            } else {
                cert.setIssuedAt(java.time.LocalDateTime.now());
            }
            cert.setStatus(com.example.orgo_project.enums.CertificateStatus.APPROVED);
            if (certFiles != null && i < certFiles.size()) {
                MultipartFile file = certFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    cert.setAttachmentFile(saveImage(file));
                }
            }
            certRepository.save(cert);
        }
    }

    // ==================== UTIL ====================

    private String saveImage(MultipartFile file) {
        try {
            String uploadDir = "src/main/resources/static/uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return "/uploads/" + filename;
        } catch (IOException e) {
            return null;
        }
    }
}
