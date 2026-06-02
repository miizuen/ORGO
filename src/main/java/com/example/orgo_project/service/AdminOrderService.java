package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderItemDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminOrderService implements IAdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderService.class);

    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IProductVariantRepository productVariantRepository;
    private final IProductRepository productRepository;
    private final IUserRepository userRepository;
    private final EmailService emailService;

    public AdminOrderService(ICustomerOrderRepository orderRepository,
                             ICustomerOrderItemRepository orderItemRepository,
                             IProductVariantRepository productVariantRepository,
                             IProductRepository productRepository,
                             IUserRepository userRepository,
                             EmailService emailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public List<OrderSummaryDTO> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Override
    public OrderDetailDTO getOrderDetail(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        List<OrderItemDTO> items = orderItemRepository.findByOrderId(orderId).stream().map(this::toItemDto).toList();
        return OrderDetailDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .shippingAddressId(order.getShippingAddressId())
                .paymentMethodId(order.getPaymentMethodId())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .note(order.getNote())
                .cancellationReason(order.getCancellationReason())
                .orderedAt(order.getOrderedAt())
                .items(items)
                .refundBankName(order.getRefundBankName())
                .refundAccountNumber(order.getRefundAccountNumber())
                .refundAccountName(order.getRefundAccountName())
                .refundTransactionCode(order.getRefundTransactionCode())
                .refundApprovedAt(order.getRefundApprovedAt())
                .build();
    }

    @Override
    public boolean approveOrder(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        log.info("Approving order {} with status payment={} order={}", order.getOrderCode(), order.getPaymentStatus(), order.getOrderStatus());
        if (order.getPaymentStatus() != PaymentStatus.PAID) throw new RuntimeException("Đơn hàng chưa thanh toán");
        if (order.getOrderStatus() != OrderStatus.PENDING) throw new RuntimeException("Chỉ được duyệt đơn ở trạng thái PENDING");
        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        log.info("Order {} marked PROCESSING, distributing revenue now", order.getOrderCode());
        log.info("Revenue distribution finished for order {}", order.getOrderCode());
        return true;
    }

    private OrderSummaryDTO toSummary(CustomerOrder order) {
        UserProfile profile = null;
        if (order.getUserId() != null) {
            profile = userRepository.findByAccount_Id(order.getUserId()).orElse(null);
        }

        List<CustomerOrderItem> items = order.getId() != null
                ? orderItemRepository.findByOrderId(order.getId())
                : List.of();

        String itemSummary = null;
        if (!items.isEmpty()) {
            CustomerOrderItem first = items.get(0);
            ProductVariant variant = first.getProductVariantId() != null
                    ? productVariantRepository.findById(first.getProductVariantId()).orElse(null)
                    : null;

            Product product = variant != null && variant.getProductId() != null
                    ? productRepository.findById(variant.getProductId()).orElse(null)
                    : null;

            String baseName = product != null ? product.getProductName() : "Sản phẩm";
            String variantName = variant != null ? variant.getVariantName() : null;
            if (variantName != null && !variantName.isBlank()) {
                baseName = baseName + " (" + variantName + ")";
            }

            int distinctLines = items.size();
            if (distinctLines > 1) {
                int extra = distinctLines - 1;
                baseName = baseName + " +" + extra + " sản phẩm";
            }
            itemSummary = baseName;
        }

        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .itemSummary(itemSummary)
                .customerName(profile != null ? profile.getFullName() : null)
                .customerPhone(profile != null ? profile.getPhoneNumber() : null)
                .cancellationReason(order.getCancellationReason())
                .refundBankName(order.getRefundBankName())
                .refundAccountNumber(order.getRefundAccountNumber())
                .refundAccountName(order.getRefundAccountName())
                .build();
    }

    private OrderItemDTO toItemDto(CustomerOrderItem item) {
        ProductVariant variant = item.getProductVariantId() != null ? productVariantRepository.findById(item.getProductVariantId()).orElse(null) : null;
        Product product = variant != null && variant.getProductId() != null ? productRepository.findById(variant.getProductId()).orElse(null) : null;
        Integer sellerId = product != null ? product.getSellerId() : null;
        return OrderItemDTO.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariantId())
                .productId(variant != null ? variant.getProductId() : null)
                .sellerId(sellerId)
                .sellerName(sellerId != null ? ("Seller #" + sellerId) : null)
                .productName(product != null ? product.getProductName() : null)
                .variantName(variant != null ? variant.getVariantName() : null)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }

    @Override
    public List<OrderSummaryDTO> getRefundRequests() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getOrderStatus() == OrderStatus.CANCELLED && 
                             (o.getPaymentStatus() == PaymentStatus.PAID || o.getPaymentStatus() == PaymentStatus.REFUNDED))
                .map(this::toSummary)
                .sorted((o1, o2) -> o2.getOrderedAt().compareTo(o1.getOrderedAt()))
                .toList();
    }

    @Override
    public boolean approveRefund(Integer orderId, String transactionCode) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getOrderStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException("Chỉ được hoàn tiền cho đơn hàng đã hủy.");
        }
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            throw new RuntimeException("Đơn hàng chưa thanh toán hoặc đã được hoàn tiền.");
        }
        if (transactionCode == null || transactionCode.isBlank()) {
            throw new RuntimeException("Mã giao dịch hoàn tiền không được để trống.");
        }

        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order.setRefundTransactionCode(transactionCode);
        order.setRefundApprovedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Gửi email
        UserProfile profile = userRepository.findByAccount_Id(order.getUserId()).orElse(null);
        if (profile != null && profile.getEmail() != null && !profile.getEmail().isBlank()) {
            try {
                emailService.sendRefundEmail(profile.getEmail(), order.getOrderCode(), order.getTotalAmount(), transactionCode);
            } catch (Exception e) {
                log.error("Failed to send refund email to: " + profile.getEmail(), e);
            }
        }
        return true;
    }
}
