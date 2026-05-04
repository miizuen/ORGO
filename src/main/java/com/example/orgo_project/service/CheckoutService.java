package com.example.orgo_project.service;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CheckoutPageDataDTO;
import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.ShippingAddress;
import com.example.orgo_project.entity.ShoppingCart;
import com.example.orgo_project.entity.ShoppingCartItem;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IShippingAddressRepository;
import com.example.orgo_project.repository.IShoppingCartItemRepository;
import com.example.orgo_project.repository.IShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CheckoutService implements ICheckoutService {

    private final IShoppingCartRepository cartRepository;
    private final IShoppingCartItemRepository cartItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductRepository productRepository;
    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IShippingAddressRepository shippingAddressRepository;

    public CheckoutService(IShoppingCartRepository cartRepository,
                           IShoppingCartItemRepository cartItemRepository,
                           IProductVariantRepository productVariantRepository,
                           IProductRepository productRepository,
                           ICustomerOrderRepository orderRepository,
                           ICustomerOrderItemRepository orderItemRepository,
                           IShippingAddressRepository shippingAddressRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingAddressRepository = shippingAddressRepository;
    }

    @Override
    public CheckoutPageDataDTO getCheckoutPageData(Integer accountId, String selectedItemIds) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart == null) {
            throw new RuntimeException("Không tìm thấy giỏ hàng");
        }

        List<ShoppingCartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        if (allItems == null || allItems.isEmpty()) {
            return CheckoutPageDataDTO.builder()
                    .items(List.of())
                    .totalAmount(BigDecimal.ZERO)
                    .shippingAddresses(List.of())
                    .defaultShippingAddress(null)
                    .build();
        }

        Set<Integer> selectedIds = parseSelectedItemIds(selectedItemIds);
        List<ShoppingCartItem> selectedItems = allItems.stream()
                .filter(item -> item.getId() != null && selectedIds.contains(item.getId()))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn sản phẩm nào");
        }

        List<CartItemDTO> checkoutItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCartItem item : selectedItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
            Product product = productRepository.findById(variant.getProductId()).orElse(null);

            BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }

            String shopName = "Nhà bán hàng";
            Integer sellerId = null;
            if (product != null) {
                sellerId = product.getSellerId();
            }

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            checkoutItems.add(CartItemDTO.builder()
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
                    .estimatedPrice(lineTotal)
                    .stockQuantity(variant.getStockQuantity())
                    .build());
        }

        List<ShippingAddress> shippingAddresses = shippingAddressRepository
                .findByAccountIdOrderByDefaultAddressDescIdDesc(accountId);
        ShippingAddress defaultShippingAddress = shippingAddresses.stream()
                .filter(addr -> Boolean.TRUE.equals(addr.getDefaultAddress()))
                .findFirst()
                .orElse(shippingAddresses.isEmpty() ? null : shippingAddresses.get(0));

        return CheckoutPageDataDTO.builder()
                .items(checkoutItems)
                .totalAmount(totalAmount)
                .shippingAddresses(shippingAddresses)
                .defaultShippingAddress(defaultShippingAddress)
                .build();
    }

    @Override
    public CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request, String selectedItemIds) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart == null) {
            throw new RuntimeException("Không tìm thấy giỏ hàng");
        }

        List<ShoppingCartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        if (allItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        Set<Integer> selectedIds = parseSelectedItemIds(selectedItemIds);
        List<ShoppingCartItem> cartItems = allItems.stream()
                .filter(item -> item.getId() != null && selectedIds.contains(item.getId()))
                .toList();

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Bạn chưa chọn sản phẩm nào");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm không đủ tồn kho");
            }

            BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal finalTotal = totalAmount;

        CustomerOrder order = new CustomerOrder();
        order.setUserId(accountId);
        order.setShippingAddressId(request != null ? request.getShippingAddressId() : null);
        order.setPaymentMethodId(request != null ? request.getPaymentMethodId() : null);
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderedAt(LocalDateTime.now());
        order.setTotalAmount(finalTotal);
        order.setShippingFee(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setOrderStatus(OrderStatus.PENDING);
        String shipperNote = request != null ? request.getShipperNote() : null;
        String shopNote = request != null ? request.getShopNote() : null;
        order.setNote(buildOrderNote(shipperNote, shopNote));

        CustomerOrder savedOrder = orderRepository.save(order);

        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            if (unitPrice == null) {
                unitPrice = BigDecimal.ZERO;
            }

            CustomerOrderItem orderItem = new CustomerOrderItem();
            orderItem.setOrderId(savedOrder.getId());
            orderItem.setProductVariantId(item.getProductVariantId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemRepository.save(orderItem);

            variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            productVariantRepository.save(variant);
        }

        cartItemRepository.deleteAll(cartItems);

        return CheckoutResponseDTO.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .totalAmount(finalTotal)
                .message("Đặt hàng thành công")
                .build();
    }

    private Set<Integer> parseSelectedItemIds(String selectedItemIds) {
        Set<Integer> selectedIds = new HashSet<>();
        if (selectedItemIds == null || selectedItemIds.isBlank()) {
            return selectedIds;
        }

        for (String idStr : selectedItemIds.split(",")) {
            if (idStr == null || idStr.isBlank()) continue;
            try {
                selectedIds.add(Integer.parseInt(idStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return selectedIds;
    }

    private String buildOrderNote(String shipperNote, String shopNote) {
        StringBuilder note = new StringBuilder();
        if (shipperNote != null && !shipperNote.isBlank()) {
            note.append("Ghi chú cho shipper: ").append(shipperNote.trim());
        }
        if (shopNote != null && !shopNote.isBlank()) {
            if (!note.isEmpty()) {
                note.append(" | ");
            }
            note.append("Ghi chú cho shop: ").append(shopNote.trim());
        }
        return note.isEmpty() ? null : note.toString();
    }
}
