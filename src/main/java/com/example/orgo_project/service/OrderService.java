package com.example.orgo_project.service;

import com.example.orgo_project.dto.OrderDetailDTO;
import com.example.orgo_project.dto.OrderItemDTO;
import com.example.orgo_project.dto.OrderSummaryDTO;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.ProductVariant;
import com.example.orgo_project.entity.Product;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.repository.ICustomerOrderItemRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.IShippingAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService implements IOrderService {

    private final ICustomerOrderRepository orderRepository;
    private final ICustomerOrderItemRepository orderItemRepository;
    private final IProductVariantRepository variantRepository;
    private final IProductRepository productRepository;
    private final ISellerRepository sellerRepository;
    private final IShippingAddressRepository shippingAddressRepository;
    private final IRevenueDistributionService revenueDistributionService;

    public OrderService(ICustomerOrderRepository orderRepository,
                        ICustomerOrderItemRepository orderItemRepository,
                        IProductVariantRepository variantRepository,
                        IProductRepository productRepository,
                        ISellerRepository sellerRepository,
                        IShippingAddressRepository shippingAddressRepository,
                        IRevenueDistributionService revenueDistributionService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.revenueDistributionService = revenueDistributionService;
    }

    @Override
    public List<OrderSummaryDTO> getMyOrders(Integer accountId) {
        return orderRepository.findByUserIdOrderByOrderedAtDesc(accountId)
                .stream().map(this::toSummary).toList();
    }

    public boolean confirmDelivery(Integer accountId, Integer orderId) {
        com.example.orgo_project.entity.CustomerOrder order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null || !order.getUserId().equals(accountId)) return false;
        if (order.getOrderStatus() != com.example.orgo_project.enums.OrderStatus.SHIPPED) return false;
        order.setOrderStatus(com.example.orgo_project.enums.OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        try {
            revenueDistributionService.distributeForOrder(orderId);
        } catch (Exception ex) {
            System.err.println("Revenue distribution failed for delivered order " + orderId + ": " + ex.getMessage());
        }
        return true;
    }

    public List<OrderSummaryDTO> getMyOrdersByStatus(Integer accountId, OrderStatus status) {
        return orderRepository.findByUserIdAndOrderStatusOrderByOrderedAtDesc(accountId, status)
                .stream().map(this::toSummary).toList();
    }

    @Override
    public OrderDetailDTO getOrderDetail(Integer accountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Lấy tên shop
        String shopName = null;
        if (order.getSellerId() != null) {
            Seller seller = sellerRepository.findById(order.getSellerId()).orElse(null);
            if (seller != null) shopName = seller.getShopName();
        }

        // Lấy địa chỉ giao hàng
        String recipientName = null, recipientPhone = null, recipientAddress = null;
        if (order.getShippingAddressId() != null) {
            com.example.orgo_project.entity.ShippingAddress addr =
                    shippingAddressRepository.findById(order.getShippingAddressId()).orElse(null);
            if (addr != null) {
                recipientName = addr.getRecipientName();
                recipientPhone = addr.getRecipientPhone();
                String detail = addr.getDetailedAddress() != null ? addr.getDetailedAddress() : "";
                String province = addr.getProvinceOrCity() != null ? ", " + addr.getProvinceOrCity() : "";
                recipientAddress = detail + province;
            }
        }

        // Lấy items với tên sản phẩm
        List<OrderItemDTO> items = orderItemRepository.findByOrderId(orderId)
                .stream().map(item -> {
                    OrderItemDTO dto = toItemDTO(item);
                    if (item.getProductVariantId() != null) {
                        ProductVariant v = variantRepository.findById(item.getProductVariantId()).orElse(null);
                        if (v != null) {
                            dto.setVariantName(v.getVariantName());
                            if (v.getProductId() != null) {
                                Product p = productRepository.findById(v.getProductId()).orElse(null);
                                if (p != null) {
                                    dto.setProductName(p.getProductName());
                                    dto.setProductId(p.getId());
                                    dto.setSellerName(p.getSlug());
                                }
                            }
                        }
                    }
                    return dto;
                }).toList();

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
                .paidAt(order.getPaidAt())
                .confirmedAt(order.getConfirmedAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .items(items)
                .shopName(shopName)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .recipientAddress(recipientAddress)
                .build();
    }

    @Override
    public boolean cancelOrder(Integer accountId, Integer orderId, String reason) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (!order.getUserId().equals(accountId))
            throw new RuntimeException("Không có quyền hủy đơn này");
        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.PROCESSING)
            throw new RuntimeException("Chỉ được hủy đơn ở trạng thái PENDING hoặc PROCESSING");
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        orderRepository.save(order);
        return true;
    }

    private OrderSummaryDTO toSummary(CustomerOrder order) {
        // Lấy tên shop
        String shopName = null;
        if (order.getSellerId() != null) {
            Seller seller = sellerRepository.findById(order.getSellerId()).orElse(null);
            if (seller != null) shopName = seller.getShopName();
        }

        // Lấy tóm tắt sản phẩm
        String itemSummary = null;
        List<CustomerOrderItem> items = orderItemRepository.findByOrderId(order.getId());
        if (!items.isEmpty()) {
            List<String> names = items.stream().map(item -> {
                if (item.getProductVariantId() != null) {
                    ProductVariant v = variantRepository.findById(item.getProductVariantId()).orElse(null);
                    if (v != null && v.getProductId() != null) {
                        Product p = productRepository.findById(v.getProductId()).orElse(null);
                        if (p != null) return p.getProductName();
                    }
                }
                return null;
            }).filter(java.util.Objects::nonNull).distinct().toList();

            if (!names.isEmpty()) {
                itemSummary = names.size() + " sản phẩm: " + String.join(", ",
                        names.size() > 2 ? names.subList(0, 2) : names)
                        + (names.size() > 2 ? "..." : "");
            }
        }

        return OrderSummaryDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .totalAmount(order.getTotalAmount())
                .orderedAt(order.getOrderedAt())
                .shopName(shopName)
                .itemSummary(itemSummary)
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
