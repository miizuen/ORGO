package com.example.orgo_project.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
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

    @Autowired
    private ICustomerOrderItemRepository orderItemRepository;

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

    public List<Product> getRandomActiveProducts(int limit) {
        return productRepository.findRandomTop4ActiveProducts();
    }

    public List<Product> getRandomActiveProductsExcluding(java.util.Set<Integer> excludedIds, int limit) {
        if (excludedIds == null || excludedIds.isEmpty()) {
            return getRandomActiveProducts(limit);
        }
        return productRepository.findRandomTop4ActiveProductsExcluding(excludedIds);
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
        Page<Product> products = productRepository.findBySellerId(sellerId, pageable);
        // Load variants for each product to display stock information
        products.getContent().forEach(product -> {
            List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
            product.setVariants(variants);
        });
        return products;
    }

    public Page<Product> getVisibleProductsBySeller(Integer sellerId, Pageable pageable) {
        return productRepository.findBySellerIdAndHiddenFalse(sellerId, pageable);
    }

    @Transactional
    public Product createProduct(Product product, List<ProductVariant> variants, MultipartFile imageFile) {
        product.setStatus(ProductStatus.PENDING);
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = saveImage(imageFile);
            product.setImageUrl(imageUrl);
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
            product.setImageUrl(imageUrl);
            product.setSlug(imageUrl);
        } else {
            product.setImageUrl(existing.getImageUrl());
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
    public void stopSellingProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setStatus(ProductStatus.INACTIVE);
            productRepository.save(p);
        }
    }

    @Transactional
    public void resumeSellingProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setStatus(ProductStatus.ACTIVE);
            productRepository.save(p);
        }
    }

    public Page<Product> getProductsBySellerWithFilters(Integer sellerId, String search, String status, Pageable pageable) {
        return getProductsBySellerWithFilters(sellerId, search, status, null, "newest", pageable);
    }

    public Page<Product> getProductsBySellerWithFilters(Integer sellerId, String search, String status, Integer categoryId, String sort, Pageable pageable) {
        List<Product> allProducts = new ArrayList<>(productRepository.findBySellerId(sellerId));
        allProducts.forEach(product -> product.setVariants(variantRepository.findByProductId(product.getId())));

        String normalizedSearch = search != null ? search.trim().toLowerCase() : "";
        boolean hasSearch = !normalizedSearch.isBlank();
        boolean hasCategoryFilter = categoryId != null && categoryId > 0;
        boolean filterAllStatus = status == null || status.isBlank() || "all".equalsIgnoreCase(status);
        ProductStatus requestedStatus = null;
        if (!filterAllStatus) {
            try {
                requestedStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ex) {
                filterAllStatus = true;
            }
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            if (hasSearch) {
                String productName = product.getProductName() != null ? product.getProductName().toLowerCase() : "";
                String origin = product.getOrigin() != null ? product.getOrigin().toLowerCase() : "";
                String sku = ("OR-" + product.getId()).toLowerCase();
                boolean matched = productName.contains(normalizedSearch)
                        || origin.contains(normalizedSearch)
                        || sku.contains(normalizedSearch);
                if (!matched) {
                    continue;
                }
            }

            if (!filterAllStatus) {
                if (product.getStatus() == null || product.getStatus() != requestedStatus) {
                    continue;
                }
            }

            if (hasCategoryFilter) {
                if (product.getCategoryId() == null || !product.getCategoryId().equals(categoryId)) {
                    continue;
                }
            }

            filtered.add(product);
        }

        Comparator<Product> comparator = Comparator.comparing(Product::getId, Comparator.nullsLast(Integer::compareTo)).reversed();
        String normalizedSort = sort != null ? sort.toLowerCase() : "newest";
        switch (normalizedSort) {
            case "oldest":
                comparator = Comparator.comparing(Product::getId, Comparator.nullsLast(Integer::compareTo));
                break;
            case "name_asc":
                comparator = Comparator.comparing(p -> p.getProductName() != null ? p.getProductName().toLowerCase() : "");
                break;
            case "name_desc":
                comparator = Comparator.comparing((Product p) -> p.getProductName() != null ? p.getProductName().toLowerCase() : "").reversed();
                break;
            case "price_asc":
                comparator = Comparator.comparing(this::getMinPrice);
                break;
            case "price_desc":
                comparator = Comparator.comparing(this::getMinPrice).reversed();
                break;
            case "stock_asc":
                comparator = Comparator.comparingInt(this::getTotalStock);
                break;
            case "stock_desc":
                comparator = Comparator.comparingInt(this::getTotalStock).reversed();
                break;
            default:
                break;
        }

        filtered.sort(comparator);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<Product> paged = start >= filtered.size() ? List.of() : filtered.subList(start, end);
        return new PageImpl<>(paged, pageable, filtered.size());
    }

    public List<Product> getAllProductsBySeller(Integer sellerId) {
        List<Product> products = new ArrayList<>(productRepository.findBySellerId(sellerId));
        products.forEach(product -> product.setVariants(variantRepository.findByProductId(product.getId())));
        return products;
    }

    public int getTotalStock(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return 0;
        }
        int totalStock = 0;
        for (ProductVariant variant : product.getVariants()) {
            if (variant.getStockQuantity() != null) {
                totalStock += variant.getStockQuantity();
            }
        }
        return totalStock;
    }

    public java.math.BigDecimal getEstimatedInventoryValue(Product product) {
        if (product == null || product.getVariants() == null || product.getVariants().isEmpty()) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (ProductVariant variant : product.getVariants()) {
            java.math.BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            Integer stock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
            if (unitPrice != null && stock > 0) {
                total = total.add(unitPrice.multiply(java.math.BigDecimal.valueOf(stock)));
            }
        }
        return total;
    }

    public java.math.BigDecimal getDisplayPrice(Product product) {
        return getMinPrice(product);
    }

    @Transactional
    public void hideProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setHidden(Boolean.TRUE);
            productRepository.save(p);
        }
    }

    @Transactional
    public void showProduct(Integer productId) {
        Product p = productRepository.findById(productId).orElse(null);
        if (p != null) {
            p.setHidden(Boolean.FALSE);
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

    public boolean hasPurchased(Integer productId, Integer userId) {
        return orderItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    public String saveReviewImage(MultipartFile file) {
        return saveImage(file);
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

    public long getActiveProductCountByCategory(Integer categoryId) {
        return productRepository.countActiveByCategoryId(categoryId);
    }

    public long countAllActive() {
        return productRepository.countAllActive();
    }

    public List<String> getAllOrigins() {
        return productRepository.findDistinctOrigins();
    }

    public Page<Product> getFilteredProducts(String keyword, Integer categoryId, Double minPrice, Double maxPrice, String origin, String sort, Pageable pageable) {
        Pageable unpaged = PageRequest.of(0, 100000);
        Page<Product> rawProducts = productRepository.searchProducts(
                (keyword != null && !keyword.isBlank()) ? keyword : null,
                categoryId,
                unpaged
        );
        
        List<Product> list = new java.util.ArrayList<>(rawProducts.getContent());
        List<Product> filteredList = new java.util.ArrayList<>();
        
        for (Product p : list) {
            List<ProductVariant> variants = variantRepository.findByProductId(p.getId());
            p.setVariants(variants);
            
            // Calculate minimum price of variants
            java.math.BigDecimal price = java.math.BigDecimal.ZERO;
            if (variants != null && !variants.isEmpty()) {
                price = variants.get(0).getDiscountedPrice() != null ? variants.get(0).getDiscountedPrice() : variants.get(0).getOriginalPrice();
                for (ProductVariant v : variants) {
                    java.math.BigDecimal vp = v.getDiscountedPrice() != null ? v.getDiscountedPrice() : v.getOriginalPrice();
                    if (vp != null && vp.compareTo(price) < 0) {
                        price = vp;
                    }
                }
            }
            
            if (minPrice != null && price.doubleValue() < minPrice) continue;
            if (maxPrice != null && price.doubleValue() > maxPrice) continue;
            
            if (origin != null && !origin.isBlank() && !origin.equalsIgnoreCase(p.getOrigin())) continue;
            
            filteredList.add(p);
        }
        
        if (sort != null) {
            if (sort.equals("price_asc")) {
                filteredList.sort((p1, p2) -> getMinPrice(p1).compareTo(getMinPrice(p2)));
            } else if (sort.equals("price_desc")) {
                filteredList.sort((p1, p2) -> getMinPrice(p2).compareTo(getMinPrice(p1)));
            } else if (sort.equals("rating")) {
                filteredList.sort((p1, p2) -> {
                    float r1 = p1.getAverageRating() != null ? p1.getAverageRating() : 0f;
                    float r2 = p2.getAverageRating() != null ? p2.getAverageRating() : 0f;
                    return Float.compare(r2, r1);
                });
            } else {
                filteredList.sort((p1, p2) -> p2.getId().compareTo(p1.getId()));
            }
        } else {
            filteredList.sort((p1, p2) -> p2.getId().compareTo(p1.getId()));
        }
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        
        List<Product> pagedList = new java.util.ArrayList<>();
        if (start <= filteredList.size()) {
            pagedList = filteredList.subList(start, end);
        }
        
        return new org.springframework.data.domain.PageImpl<>(pagedList, pageable, filteredList.size());
    }
    
    private java.math.BigDecimal getMinPrice(Product p) {
        List<ProductVariant> variants = p.getVariants();
        if (variants == null || variants.isEmpty()) return java.math.BigDecimal.ZERO;
        java.math.BigDecimal min = variants.get(0).getDiscountedPrice() != null ? variants.get(0).getDiscountedPrice() : variants.get(0).getOriginalPrice();
        for (ProductVariant v : variants) {
            java.math.BigDecimal vp = v.getDiscountedPrice() != null ? v.getDiscountedPrice() : v.getOriginalPrice();
            if (vp != null && vp.compareTo(min) < 0) {
                min = vp;
            }
        }
        return min;
    }
}
