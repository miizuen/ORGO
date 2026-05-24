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
    private final com.example.orgo_project.repository.IUserProfileRepository userProfileRepository;

    public OrderService(ICustomerOrderRepository orderRepository,
                        ICustomerOrderItemRepository orderItemRepository,
                        IProductVariantRepository variantRepository,
                        IProductRepository productRepository,
                        ISellerRepository sellerRepository,
                        IShippingAddressRepository shippingAddressRepository,
                        IRevenueDistributionService revenueDistributionService,
                        com.example.orgo_project.repository.IUserProfileRepository userProfileRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.variantRepository = variantRepository;
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.revenueDistributionService = revenueDistributionService;
        this.userProfileRepository = userProfileRepository;
    }
    
    /**
     * Lấy danh sách ID tương thích để query đơn hàng (hỗ trợ cả dữ liệu cũ và mới)
     * - Dữ liệu cũ: id_nguoi_dung = id_tai_khoan (sai)
     * - Dữ liệu mới: id_nguoi_dung = UserProfile.id (đúng)
     */
    private java.util.List<Integer> getCompatibleUserIds(Integer accountId) {
        java.util.List<Integer> ids = new java.util.ArrayList<>();
        
        // Thêm accountId (cho dữ liệu cũ lưu sai)
        ids.add(accountId);
        
        // Thêm userProfileId (cho dữ liệu mới lưu đúng)
        userProfileRepository.findByAccountId(accountId).ifPresent(profile -> {
            if (profile.getId() != null && !ids.contains(profile.getId())) {
                ids.add(profile.getId());
            }
        });
        
        return ids;
    }

    @Override
    public List<OrderSummaryDTO> getMyOrders(Integer accountId) {
        // ✅ Query theo cả accountId và userProfileId để hỗ trợ dữ liệu cũ + mới
        java.util.List<Integer> compatibleIds = getCompatibleUserIds(accountId);
        return orderRepository.findByUserIdInOrderByOrderedAtDesc(compatibleIds)
                .stream().map(this::toSummary).toList();
    }

    @Override
    public boolean confirmDelivery(Integer accountId, Integer orderId) {
        com.example.orgo_project.entity.CustomerOrder order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null) return false;
        
        // ✅ Kiểm tra quyền với cả accountId và userProfileId
        java.util.List<Integer> compatibleIds = getCompatibleUserIds(accountId);
        if (!compatibleIds.contains(order.getUserId())) return false;
        if (order.getOrderStatus() != com.example.orgo_project.enums.OrderStatus.SHIPPED) return false;
        
        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(com.example.orgo_project.enums.OrderStatus.DELIVERED);
        order.setDeliveredAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        
        // ✅ KÍCH HOẠT CHIA TIỀN TỰ ĐỘNG khi user xác nhận đã nhận hàng
        try {
            revenueDistributionService.distributeForOrder(orderId);
            System.out.println("✅ Revenue distributed successfully for order: " + order.getOrderCode());
        } catch (Exception e) {
            System.err.println("❌ Failed to distribute revenue for order " + order.getOrderCode() + ": " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến việc xác nhận nhận hàng
            // Admin có thể chia tiền thủ công sau
        }
        
        return true;
    }

    @Override
    public List<OrderSummaryDTO> getMyOrdersByStatus(Integer accountId, OrderStatus status) {
        // ✅ Query theo cả accountId và userProfileId để hỗ trợ dữ liệu cũ + mới
        java.util.List<Integer> compatibleIds = getCompatibleUserIds(accountId);
        return orderRepository.findByUserIdInAndOrderStatusOrderByOrderedAtDesc(compatibleIds, status)
                .stream().map(this::toSummary).toList();
    }

    @Override
    public OrderDetailDTO getOrderDetail(Integer accountId, Integer orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // ✅ Kiểm tra quyền truy cập với cả accountId và userProfileId
        java.util.List<Integer> compatibleIds = getCompatibleUserIds(accountId);
        if (!compatibleIds.contains(order.getUserId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

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
        
        // ✅ Kiểm tra quyền với cả accountId và userProfileId
        java.util.List<Integer> compatibleIds = getCompatibleUserIds(accountId);
        if (!compatibleIds.contains(order.getUserId())) {
            throw new RuntimeException("Không có quyền hủy đơn này");
        }
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
            }).filter(n -> n != null).distinct().collect(Collectors.toList());

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
