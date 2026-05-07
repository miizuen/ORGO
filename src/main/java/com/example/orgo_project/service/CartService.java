package com.example.orgo_project.service;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CartSummaryDTO;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.entity.ShoppingCart;
import com.example.orgo_project.entity.ShoppingCartItem;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.IShoppingCartItemRepository;
import com.example.orgo_project.repository.IShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class CartService implements ICartService {

    private final IShoppingCartRepository cartRepository;
    private final IShoppingCartItemRepository cartItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductRepository productRepository;
    private final ISellerRepository sellerRepository;

    public CartService(IShoppingCartRepository cartRepository,
                       IShoppingCartItemRepository cartItemRepository,
                       IProductVariantRepository productVariantRepository,
                       IProductRepository productRepository,
                       ISellerRepository sellerRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public CartSummaryDTO getMyCart(Integer accountId) {
        ShoppingCart cart = getOrCreateCart(accountId);
        List<CartItemDTO> items = getCartItems(accountId);

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getEstimatedPrice() == null ? BigDecimal.ZERO : item.getEstimatedPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItems = items.stream()
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();

        return CartSummaryDTO.builder()
                .cartId(cart.getId())
                .items(items)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }

    @Override
    public CartItemDTO addItem(Integer accountId, Integer productVariantId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;

        ShoppingCart cart = getOrCreateCart(accountId);

        ProductVariant variant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm"));

        ShoppingCartItem existing = cartItemRepository.findByCartIdAndProductVariantId(cart.getId(), productVariantId);
        if (existing == null) {
            existing = new ShoppingCartItem();
            existing.setCartId(cart.getId());
            existing.setProductVariantId(productVariantId);
            existing.setQuantity(0);
        }

        int newQty = existing.getQuantity() + quantity;
        if (variant.getStockQuantity() != null && newQty > variant.getStockQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }

        existing.setQuantity(newQty);
        existing.setEstimatedPrice(calculatePrice(variant, newQty));
        cartItemRepository.save(existing);

        return toDTO(existing, variant);
    }

    @Override
    public CartItemDTO updateItem(Integer accountId, Integer cartItemId, Integer quantity) {
        ShoppingCart cart = getOrCreateCart(accountId);

        ShoppingCartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));

        if (!cart.getId().equals(item.getCartId())) {
            throw new IllegalArgumentException("Không có quyền sửa sản phẩm này");
        }

        if (quantity == null || quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm"));

        if (variant.getStockQuantity() != null && quantity > variant.getStockQuantity()) {
            throw new IllegalArgumentException("Số lượng vượt quá tồn kho");
        }

        item.setQuantity(quantity);
        item.setEstimatedPrice(calculatePrice(variant, quantity));
        cartItemRepository.save(item);

        return toDTO(item, variant);
    }

    @Override
    public boolean removeItem(Integer accountId, Integer cartItemId) {
        ShoppingCart cart = getOrCreateCart(accountId);
        ShoppingCartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item == null || !cart.getId().equals(item.getCartId())) return false;

        cartItemRepository.delete(item);
        return true;
    }

    @Override
    public void clearCart(Integer accountId) {
        ShoppingCart cart = getOrCreateCart(accountId);
        cartItemRepository.deleteByCartId(cart.getId());
    }

    @Override
    public List<CartItemDTO> getCartItems(Integer accountId) {
        ShoppingCart cart = getOrCreateCart(accountId);
        List<ShoppingCartItem> items = cartItemRepository.findByCartId(cart.getId());

        if (items.isEmpty()) return Collections.emptyList();

        List<CartItemDTO> result = new ArrayList<>();
        for (ShoppingCartItem item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
            if (variant != null) {
                result.add(toDTO(item, variant));
            }
        }
        return result;
    }

    private ShoppingCart getOrCreateCart(Integer accountId) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart != null) return cart;

        cart = new ShoppingCart();
        cart.setAccountId(accountId);
        return cartRepository.save(cart);
    }

    private BigDecimal calculatePrice(ProductVariant variant, Integer quantity) {
        BigDecimal price = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
        if (price == null) price = BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private CartItemDTO toDTO(ShoppingCartItem item, ProductVariant variant) {
        Product product = productRepository.findById(variant.getProductId()).orElse(null);

        BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;

        Integer sellerId = null;
        String shopName = "Nhà bán hàng";

        if (product != null && product.getSellerId() != null) {
            sellerId = product.getSellerId();

            Seller seller = sellerRepository.findById(sellerId).orElse(null);
            if (seller != null && seller.getShopName() != null && !seller.getShopName().isBlank()) {
                shopName = seller.getShopName();
            }
        }

        return CartItemDTO.builder()
                .id(item.getId())
                .cartId(item.getCartId())
                .productVariantId(item.getProductVariantId())
                .productId(product != null ? product.getId() : null)
                .sellerId(sellerId)
                .shopName(shopName)
                .productName(product != null ? product.getProductName() : "Sản phẩm")
                .variantName(variant.getVariantName())
                .imageUrl(product != null && product.getImageUrl() != null ? product.getImageUrl() : variant.getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitPrice)
                .estimatedPrice(item.getEstimatedPrice())
                .stockQuantity(variant.getStockQuantity())
                .build();
    }
}