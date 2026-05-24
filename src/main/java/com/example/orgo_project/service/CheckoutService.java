package com.example.orgo_project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.orgo_project.dto.CartItemDTO;
import com.example.orgo_project.dto.CheckoutPageDataDTO;
import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.PaymentHistory;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.ShippingAddress;
import com.example.orgo_project.entity.ShoppingCart;
import com.example.orgo_project.entity.ShoppingCartItem;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IPaymentHistoryRepository;
import com.example.orgo_project.repository.IPaymentQrSessionRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IShippingAddressRepository;
import com.example.orgo_project.repository.IShoppingCartItemRepository;
import com.example.orgo_project.repository.IShoppingCartRepository;

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
    private final IPaymentQrSessionRepository paymentQrSessionRepository;
    private final IPaymentHistoryRepository paymentHistoryRepository;
    private final PaymentQrService paymentQrService;
    private final IRevenueDistributionService revenueDistributionService;
    private final MomoPaymentService momoPaymentService;
    private final com.example.orgo_project.repository.IUserProfileRepository userProfileRepository;

    public CheckoutService(IShoppingCartRepository cartRepository,
                           IShoppingCartItemRepository cartItemRepository,
                           IProductVariantRepository productVariantRepository,
                           IProductRepository productRepository,
                           ICustomerOrderRepository orderRepository,
                           ICustomerOrderItemRepository orderItemRepository,
                           IShippingAddressRepository shippingAddressRepository,
                           IPaymentQrSessionRepository paymentQrSessionRepository,
                           IPaymentHistoryRepository paymentHistoryRepository,
                           PaymentQrService paymentQrService,
                           IRevenueDistributionService revenueDistributionService,
                           MomoPaymentService momoPaymentService,
                           com.example.orgo_project.repository.IUserProfileRepository userProfileRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.paymentQrSessionRepository = paymentQrSessionRepository;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.paymentQrService = paymentQrService;
        this.revenueDistributionService = revenueDistributionService;
        this.momoPaymentService = momoPaymentService;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public CheckoutPageDataDTO getCheckoutPageData(Integer accountId, String selectedItemIds) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart == null) throw new RuntimeException("Không tìm thấy giỏ hàng");
        List<ShoppingCartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        if (allItems == null || allItems.isEmpty()) return emptyCheckoutPage();

        List<ShoppingCartItem> selectedItems = selectCartItems(allItems, selectedItemIds);
        if (selectedItems.isEmpty()) throw new RuntimeException("Bạn chưa chọn sản phẩm nào");

        List<CartItemDTO> checkoutItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCartItem item : selectedItems) {
            checkoutItems.add(toCartItemDto(item));
            totalAmount = totalAmount.add(calculateLineTotal(item));
        }

        List<ShippingAddress> shippingAddresses = shippingAddressRepository.findByAccountIdOrderByDefaultAddressDescIdDesc(accountId);
        ShippingAddress defaultShippingAddress = shippingAddresses.stream().filter(addr -> Boolean.TRUE.equals(addr.getDefaultAddress())).findFirst().orElse(shippingAddresses.isEmpty() ? null : shippingAddresses.get(0));
        return CheckoutPageDataDTO.builder().items(checkoutItems).totalAmount(totalAmount).shippingAddresses(shippingAddresses).defaultShippingAddress(defaultShippingAddress).build();
    }

    @Override
    public CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request, String selectedItemIds) {
        return checkout(accountId, request, selectedItemIds, null);
    }

    @Override
    public CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request, String selectedItemIds, Integer articleId) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart == null) throw new RuntimeException("Không tìm thấy giỏ hàng");
        List<ShoppingCartItem> allItems = cartItemRepository.findByCartId(cart.getId());
        if (allItems.isEmpty()) throw new RuntimeException("Giỏ hàng đang trống");

        List<ShoppingCartItem> cartItems = selectCartItems(allItems, selectedItemIds);
        if (cartItems.isEmpty()) throw new RuntimeException("Bạn chưa chọn sản phẩm nào");

        BigDecimal totalAmount = calculateTotal(cartItems);
        Integer sellerId = resolveOrderSellerId(cartItems);
        CustomerOrder savedOrder = saveOrder(accountId, request, totalAmount, sellerId, articleId);
        saveOrderItems(savedOrder, cartItems);
        cartItemRepository.deleteAll(cartItems);

        var momoResponse = momoPaymentService.createPayment(savedOrder.getId(), savedOrder.getOrderCode(), "Thanh toan don hang " + savedOrder.getOrderCode(), totalAmount.longValue());
        if (momoResponse == null || momoResponse.getPayUrl() == null || momoResponse.getPayUrl().isBlank()) {
            throw new RuntimeException("MoMo chưa trả về payUrl hợp lệ");
        }

        return CheckoutResponseDTO.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .totalAmount(totalAmount)
                .message("Đơn hàng đã được tạo và chuyển sang MoMo để thanh toán")
                .payUrl(momoResponse.getPayUrl())
                .build();
    }

    @Override
    public CheckoutResponseDTO confirmPayment(Integer orderId, String transactionCode) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return buildPaymentResponse(order, "Đơn hàng đã được xác nhận thanh toán");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(now);
        order.setOrderStatus(OrderStatus.PENDING); // đã thanh toán xong, chờ seller duyệt
        orderRepository.save(order);

        paymentQrSessionRepository.findByOrderId(orderId).ifPresent(session -> {
            session.setStatus("PAID");
            paymentQrSessionRepository.save(session);
        });
        savePaymentHistory(order, transactionCode);

        // chia tiền ngay sau khi thanh toán thành công
        revenueDistributionService.distributeForOrder(orderId);

        return buildPaymentResponse(
                order,
                transactionCode != null && !transactionCode.isBlank()
                        ? "Thanh toán thành công: " + transactionCode
                        : "Thanh toán thành công"
        );
    }

    private CheckoutPageDataDTO emptyCheckoutPage() {
        return CheckoutPageDataDTO.builder().items(List.of()).totalAmount(BigDecimal.ZERO).shippingAddresses(List.of()).defaultShippingAddress(null).build();
    }

    private List<ShoppingCartItem> selectCartItems(List<ShoppingCartItem> allItems, String selectedItemIds) {
        Set<Integer> selectedIds = parseSelectedItemIds(selectedItemIds);
        if (selectedIds.isEmpty()) {
            return allItems;
        }
        return allItems.stream().filter(item -> item.getId() != null && selectedIds.contains(item.getId())).toList();
    }

    private CartItemDTO toCartItemDto(ShoppingCartItem item) {
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
        Product product = productRepository.findById(variant.getProductId()).orElse(null);
        BigDecimal unitPrice = resolveUnitPrice(variant);
        return CartItemDTO.builder().id(item.getId()).cartId(item.getCartId()).productVariantId(item.getProductVariantId()).productId(product != null ? product.getId() : null).sellerId(product != null ? product.getSellerId() : null).shopName("Nhà bán hàng").productName(product != null ? product.getProductName() : "Sản phẩm").variantName(variant.getVariantName()).imageUrl(product != null && product.getImageUrl() != null ? product.getImageUrl() : variant.getImageUrl()).quantity(item.getQuantity()).unitPrice(unitPrice).estimatedPrice(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity()))).stockQuantity(variant.getStockQuantity()).build();
    }

    private BigDecimal calculateLineTotal(ShoppingCartItem item) {
        ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
        BigDecimal unitPrice = resolveUnitPrice(variant);
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity() == null ? 0 : item.getQuantity()));
    }

    private BigDecimal calculateTotal(List<ShoppingCartItem> cartItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
            if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) throw new RuntimeException("Sản phẩm không đủ tồn kho");
            totalAmount = totalAmount.add(resolveUnitPrice(variant).multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return totalAmount;
    }

    private BigDecimal resolveUnitPrice(ProductVariant variant) {
        BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
        return unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    private Integer resolveOrderSellerId(List<ShoppingCartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) return null;
        ShoppingCartItem firstItem = cartItems.get(0);
        if (firstItem.getProductVariantId() == null) return null;
        ProductVariant variant = productVariantRepository.findById(firstItem.getProductVariantId()).orElse(null);
        if (variant == null || variant.getProductId() == null) return null;
        Product product = productRepository.findById(variant.getProductId()).orElse(null);
        return product != null ? product.getSellerId() : null;
    }

    private CustomerOrder saveOrder(Integer accountId, CheckoutRequestDTO request, BigDecimal totalAmount, Integer sellerId, Integer articleId) {
        // ✅ Map accountId → userProfileId (id_nguoi_dung)
        Integer userProfileId = userProfileRepository.findByAccountId(accountId)
                .map(com.example.orgo_project.entity.UserProfile::getId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));
        
        CustomerOrder order = new CustomerOrder();
        order.setUserId(userProfileId); // ✅ ĐÚNG - lưu id_nguoi_dung
        order.setSellerId(sellerId);
        order.setShippingAddressId(request != null ? request.getShippingAddressId() : null);
        order.setPaymentMethodId(request != null ? request.getPaymentMethodId() : null);
        order.setOrderCode("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderedAt(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setShippingFee(BigDecimal.ZERO);
        // Trạng thái thanh toán: PENDING (chờ thanh toán qua MoMo)
        order.setPaymentStatus(PaymentStatus.PENDING);
        // Trạng thái đơn hàng: PENDING_PAYMENT (chờ thanh toán)
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT);
        order.setNote(buildOrderNote(request != null ? request.getShipperNote() : null, request != null ? request.getShopNote() : null));
        order.setArticleId(articleId);
        return orderRepository.save(order);
    }

    private void saveOrderItems(CustomerOrder savedOrder, List<ShoppingCartItem> cartItems) {
        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));
            BigDecimal unitPrice = resolveUnitPrice(variant);
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
    }

    private void upsertPaymentQrSession(CustomerOrder savedOrder, BigDecimal totalAmount) {
        paymentQrSessionRepository.findByOrderId(savedOrder.getId()).ifPresent(existing -> paymentQrSessionRepository.delete(existing));
        paymentQrService.createQrSession(savedOrder.getId(), totalAmount);
    }

    private CheckoutResponseDTO buildPaymentResponse(CustomerOrder order, String message) {
        return CheckoutResponseDTO.builder().orderId(order.getId()).orderCode(order.getOrderCode()).totalAmount(order.getTotalAmount()).message(message).build();
    }

    private void savePaymentHistory(CustomerOrder order, String transactionCode) {
        PaymentHistory history = new PaymentHistory();
        history.setOrderId(order.getId());
        history.setPaymentMethodId(order.getPaymentMethodId());
        history.setTransactionCode(transactionCode != null && !transactionCode.isBlank() ? transactionCode.trim() : "MANUAL-" + order.getOrderCode());
        history.setAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        history.setStatus(PaymentStatus.PAID);
        history.setTransactionAt(LocalDateTime.now());
        history.setNote("Thanh toan don hang " + order.getOrderCode());
        paymentHistoryRepository.save(history);
    }


    private Set<Integer> parseSelectedItemIds(String selectedItemIds) { Set<Integer> selectedIds = new HashSet<>(); if (selectedItemIds == null || selectedItemIds.isBlank()) return selectedIds; for (String idStr : selectedItemIds.split(",")) { if (idStr == null || idStr.isBlank()) continue; try { selectedIds.add(Integer.parseInt(idStr.trim())); } catch (NumberFormatException ignored) {} } return selectedIds; }
    private String buildOrderNote(String shipperNote, String shopNote) { StringBuilder note = new StringBuilder(); if (shipperNote != null && !shipperNote.isBlank()) note.append("Ghi chú cho shipper: ").append(shipperNote.trim()); if (shopNote != null && !shopNote.isBlank()) { if (!note.isEmpty()) note.append(" | "); note.append("Ghi chú cho shop: ").append(shopNote.trim()); } return note.isEmpty() ? null : note.toString(); }
}
