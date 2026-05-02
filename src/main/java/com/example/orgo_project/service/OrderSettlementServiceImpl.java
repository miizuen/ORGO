package com.example.orgo_project.service;

import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.CustomerOrderItem;
import com.example.orgo_project.entity.EscrowBalance;
import com.example.orgo_project.repository.IOrderItemRepository;
import com.example.orgo_project.repository.IOrderRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderSettlementServiceImpl implements OrderSettlementService {

    @Autowired
    private EscrowService escrowService;

    @Autowired
    private PaymentQrService paymentQrService;

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private IOrderItemRepository orderItemRepository;

    @Autowired
    private IProductVariantRepository productVariantRepository;

    @Autowired
    private IProductRepository productRepository;

    @Override
    public EscrowBalance settleOrder(Integer orderId) {
        CustomerOrder order = orderRepository.findById(java.util.Objects.requireNonNull(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        List<CustomerOrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, BigDecimal> sellerTotals = new HashMap<>();

        for (CustomerOrderItem item : items) {
            BigDecimal lineTotal = item.getLineTotal() == null ? BigDecimal.ZERO : item.getLineTotal();
            total = total.add(lineTotal);

            Integer sellerId = resolveSellerId(item);
            if (sellerId != null) {
                sellerTotals.put(sellerId, sellerTotals.getOrDefault(sellerId, BigDecimal.ZERO).add(lineTotal));
            }
        }

        BigDecimal commission = total.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal sellerPayout = total.subtract(commission);
        BigDecimal adminRevenue = commission;

        paymentQrService.createQrSession(orderId, total);
        EscrowBalance escrow = escrowService.createEscrowForOrder(orderId, total, commission, sellerPayout, adminRevenue);
        order.setPaymentStatus(com.example.orgo_project.enums.PaymentStatus.PAID);
        orderRepository.save(order);
        escrowService.settleOrder(orderId, sellerTotals);
        return escrow;
    }

    private Integer resolveSellerId(CustomerOrderItem item) {
        if (item.getProductVariantId() == null) {
            return null;
        }
        var variant = productVariantRepository.findById(item.getProductVariantId()).orElse(null);
        if (variant == null || variant.getProductId() == null) {
            return null;
        }
        var product = productRepository.findById(variant.getProductId()).orElse(null);
        return product != null ? product.getSellerId() : null;
    }
}
