package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderItemDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SellerOrderService implements ISellerOrderService {

    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IProductVariantRepository productVariantRepository;

    public SellerOrderService(ICustomerOrderRepository orderRepository,
                              ICustomerOrderItemRepository orderItemRepository,
                              IProductVariantRepository productVariantRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public List<OrderSummaryDTO> getSellerOrders(Integer sellerAccountId) {
        List<CustomerOrder> orders = orderRepository.findAll();
        List<OrderSummaryDTO> result = new ArrayList<>();

        for (CustomerOrder order : orders) {
            if (isOrderRelatedToSeller(order.getId(), sellerAccountId)) {
                result.add(toSummary(order));
            }
        }

        return result;
    }

    @Override
    public OrderDetailDTO getSellerOrderDetail(Integer sellerAccountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!isOrderRelatedToSeller(orderId, sellerAccountId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        List<OrderItemDTO> items = orderItemRepository.findByOrderId(orderId)
                .stream()
                .map(this::toItemDTO)
                .toList();

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
    public boolean confirmOrder(Integer sellerAccountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!isOrderRelatedToSeller(orderId, sellerAccountId)) {
            throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ được xác nhận đơn ở trạng thái PENDING");
        }

        order.setOrderStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        return true;
    }

    @Override
    public boolean shipOrder(Integer sellerAccountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!isOrderRelatedToSeller(orderId, sellerAccountId)) {
            throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Chỉ được chuyển sang giao hàng khi đơn ở trạng thái PROCESSING");
        }

        order.setOrderStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        return true;
    }

    @Override
    public boolean deliverOrder(Integer sellerAccountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!isOrderRelatedToSeller(orderId, sellerAccountId)) {
            throw new RuntimeException("Bạn không có quyền thao tác đơn hàng này");
        }

        if (order.getOrderStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Chỉ được hoàn tất đơn khi đơn ở trạng thái SHIPPED");
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
        return true;
    }

    private boolean isOrderRelatedToSeller(Integer orderId, Integer sellerAccountId) {
        List<CustomerOrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) return false;

        // Logic hiện tại là tạm thời.
        // Nếu bạn có mapping ProductVariant -> Product -> sellerId thì nên thay logic này bằng kiểm tra chuẩn.
        for (CustomerOrderItem item : items) {
            ProductVariant variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
            if (variant != null) {
                return true;
            }
        }
        return false;
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

    private OrderItemDTO toItemDTO(CustomerOrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productVariantId(item.getProductVariantId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}