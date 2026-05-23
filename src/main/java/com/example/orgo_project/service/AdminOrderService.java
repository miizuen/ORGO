package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderItemDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.PaymentStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
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

    public AdminOrderService(ICustomerOrderRepository orderRepository,
                             ICustomerOrderItemRepository orderItemRepository,
                             IProductVariantRepository productVariantRepository,
                             IProductRepository productRepository,
                             IRevenueDistributionService revenueDistributionService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
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
        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
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
}
