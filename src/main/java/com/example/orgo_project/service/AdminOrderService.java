package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderItemDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.dto.ReturnRequestDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.RefundRequest;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.enums.RefundStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IRefundRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminOrderService implements IAdminOrderService {

    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IRefundRequestRepository refundRequestRepository;

    public AdminOrderService(ICustomerOrderRepository orderRepository,
                             ICustomerOrderItemRepository orderItemRepository,
                             IRefundRequestRepository refundRequestRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.refundRequestRepository = refundRequestRepository;
    }

    @Override
    public List<OrderSummaryDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public OrderDetailDTO getOrderDetail(Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

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
    public List<ReturnRequestDTO> getReturnRequests() {
        return refundRequestRepository.findAll()
                .stream()
                .map(refund -> new ReturnRequestDTO(
                        refund.getOrderId(),
                        refund.getReason(),
                        refund.getEvidenceImage()
                ))
                .toList();
    }

    @Override
    public boolean approveReturn(Integer returnId) {
        RefundRequest refund = refundRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu hoàn trả"));

        refund.setStatus(RefundStatus.APPROVED);
        refund.setUpdatedAt(LocalDateTime.now());
        refundRequestRepository.save(refund);

        CustomerOrder order = orderRepository.findById(refund.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        order.setOrderStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        return true;
    }

    @Override
    public boolean rejectReturn(Integer returnId) {
        RefundRequest refund = refundRequestRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu hoàn trả"));

        refund.setStatus(RefundStatus.REJECTED);
        refund.setUpdatedAt(LocalDateTime.now());
        refundRequestRepository.save(refund);
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