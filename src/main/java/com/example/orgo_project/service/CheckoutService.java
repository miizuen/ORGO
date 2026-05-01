package com.example.orgo_project.service;

import com.example.orgo_project.dto.CheckoutRequestDTO;
import com.example.orgo_project.dto.CheckoutResponseDTO;
import com.example.orgo_project.dto.CouponValidateRequestDTO;
import com.example.orgo_project.dto.CouponValidateResponseDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.ShoppingCart;
import com.example.orgo_project.entity.ShoppingCartItem;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IShoppingCartItemRepository;
import com.example.orgo_project.repository.IShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CheckoutService implements ICheckoutService {

    private final IShoppingCartRepository cartRepository;
    private final IShoppingCartItemRepository cartItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final ICouponService couponService;

    public CheckoutService(IShoppingCartRepository cartRepository,
                           IShoppingCartItemRepository cartItemRepository,
                           IProductVariantRepository productVariantRepository,
                           ICustomerOrderRepository orderRepository,
                           ICustomerOrderItemRepository orderItemRepository,
                           ICouponService couponService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.couponService = couponService;
    }

    @Override
    public CheckoutResponseDTO checkout(Integer accountId, CheckoutRequestDTO request) {
        ShoppingCart cart = cartRepository.findByAccountId(accountId);
        if (cart == null) throw new RuntimeException("Không tìm thấy giỏ hàng");

        List<ShoppingCartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) throw new RuntimeException("Giỏ hàng đang trống");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Sản phẩm không đủ tồn kho");
            }

            BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            if (unitPrice == null) unitPrice = BigDecimal.ZERO;

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request != null && request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            CouponValidateResponseDTO couponResult = couponService.validateCoupon(
                    new CouponValidateRequestDTO(request.getCouponCode(), totalAmount)
            );
            if (!couponResult.isValid()) {
                throw new RuntimeException(couponResult.getMessage());
            }
            discountAmount = couponResult.getDiscountAmount();
        }

        BigDecimal finalTotal = totalAmount.subtract(discountAmount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

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
        order.setNote(request != null ? request.getNote() : null);

        CustomerOrder savedOrder = orderRepository.save(order);

        for (ShoppingCartItem item : cartItems) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm"));

            BigDecimal unitPrice = variant.getDiscountedPrice() != null ? variant.getDiscountedPrice() : variant.getOriginalPrice();
            if (unitPrice == null) unitPrice = BigDecimal.ZERO;

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

        cartItemRepository.deleteByCartId(cart.getId());

        return CheckoutResponseDTO.builder()
                .orderId(savedOrder.getId())
                .orderCode(savedOrder.getOrderCode())
                .totalAmount(finalTotal)
                .message("Đặt hàng thành công")
                .build();
    }
}