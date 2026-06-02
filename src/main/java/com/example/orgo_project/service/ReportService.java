package com.example.orgo_project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.orgo_project.dto.ReportStatsDTO;
import com.example.orgo_project.dto.TopSellerDTO;
import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.CustomerOrder;
import com.example.orgo_project.entity.OrderSettlement;
import com.example.orgo_project.entity.Seller;
import com.example.orgo_project.entity.UserProfile;
import com.example.orgo_project.enums.OrderStatus;
import com.example.orgo_project.repository.IAccountRepository;
import com.example.orgo_project.repository.ICustomerOrderRepository;
import com.example.orgo_project.repository.IOrderSettlementRepository;
import com.example.orgo_project.repository.IProductRepository;
import com.example.orgo_project.repository.IProductReviewRepository;
import com.example.orgo_project.repository.ISellerRepository;
import com.example.orgo_project.repository.IUserProfileRepository;

@Service
public class ReportService {
    
    private final ICustomerOrderRepository orderRepository;
    private final IOrderSettlementRepository settlementRepository;
    private final ISellerRepository sellerRepository;
    private final IUserProfileRepository userRepository;
    private final IProductRepository productRepository;
    private final IAccountRepository accountRepository;
    private final IProductReviewRepository reviewRepository;
    
    public ReportService(ICustomerOrderRepository orderRepository,
                        IOrderSettlementRepository settlementRepository,
                        ISellerRepository sellerRepository,
                        IUserProfileRepository userRepository,
                        IProductRepository productRepository,
                        IAccountRepository accountRepository,
                        IProductReviewRepository reviewRepository) {
        this.orderRepository = orderRepository;
        this.settlementRepository = settlementRepository;
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.accountRepository = accountRepository;
        this.reviewRepository = reviewRepository;
    }
    
    public ReportStatsDTO getReportStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<CustomerOrder> allOrders = orderRepository.findAll();
        List<CustomerOrder> periodOrders = allOrders.stream()
            .filter(o -> o.getOrderedAt() != null && 
                        o.getOrderedAt().isAfter(startDate) && 
                        o.getOrderedAt().isBefore(endDate))
            .collect(Collectors.toList());
        
        // Revenue Stats
        BigDecimal totalRevenue = periodOrders.stream()
            .filter(o -> o.getTotalAmount() != null)
            .map(CustomerOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<OrderSettlement> settlements = settlementRepository.findAll();
        BigDecimal totalCommission = settlements.stream()
            .filter(s -> s.getCommissionAmount() != null)
            .map(OrderSettlement::getCommissionAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer totalOrders = periodOrders.size();
        
        // Seller Stats
        List<Seller> allSellers = sellerRepository.findAll();
        Integer totalSellers = allSellers.size();
        Integer activeSellers = (int) allSellers.stream()
            .filter(s -> s.getStatus() != null && s.getStatus().name().equals("ACTIVE"))
            .count();
        
        // Calculate avg delivery time
        Double avgDeliveryTime = periodOrders.stream()
            .filter(o -> o.getOrderedAt() != null && o.getDeliveredAt() != null)
            .mapToDouble(o -> java.time.Duration.between(o.getOrderedAt(), o.getDeliveredAt()).toHours())
            .average()
            .orElse(0.0);
        
        // On-time delivery rate
        long onTimeOrders = periodOrders.stream()
            .filter(o -> o.getOrderStatus() == OrderStatus.DELIVERED)
            .count();
        Double onTimeRate = totalOrders > 0 ? (onTimeOrders * 100.0 / totalOrders) : 0.0;
        
        // Customer Stats
        List<UserProfile> allUsers = userRepository.findAll();
        Integer totalUsers = allUsers.size();
        
        // Revenue Trend Data (last 7 days)
        List<String> revenueLabels = new ArrayList<>();
        List<BigDecimal> revenueData = new ArrayList<>();
        List<BigDecimal> orderCountData = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = LocalDateTime.now().minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            List<CustomerOrder> dayOrders = allOrders.stream()
                .filter(o -> o.getOrderedAt() != null && 
                            o.getOrderedAt().isAfter(dayStart) && 
                            o.getOrderedAt().isBefore(dayEnd))
                .collect(Collectors.toList());
            
            BigDecimal dayRevenue = dayOrders.stream()
                .filter(o -> o.getTotalAmount() != null)
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            revenueLabels.add(dayStart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            revenueData.add(dayRevenue);
            orderCountData.add(new BigDecimal(dayOrders.size()));
        }
        
        // Category Revenue (by seller)
        Map<String, BigDecimal> categoryRevenue = new HashMap<>();
        for (CustomerOrder order : periodOrders) {
            if (order.getSellerId() != null && order.getTotalAmount() != null) {
                String category = "Seller #" + order.getSellerId();
                categoryRevenue.merge(category, order.getTotalAmount(), BigDecimal::add);
            }
        }
        
        // Payment Methods (mock data - can be enhanced)
        Map<String, Integer> paymentMethods = new HashMap<>();
        paymentMethods.put("COD", (int)(totalOrders * 0.45));
        paymentMethods.put("Card", (int)(totalOrders * 0.35));
        paymentMethods.put("Wallet", (int)(totalOrders * 0.15));
        paymentMethods.put("Bank", (int)(totalOrders * 0.05));
        
        // Top Sellers
        List<TopSellerDTO> topSellers = getTopSellers(periodOrders);
        
        return ReportStatsDTO.builder()
            .totalRevenue(totalRevenue)
            .totalCommission(totalCommission)
            .totalRefunds(BigDecimal.ZERO)
            .totalOrders(totalOrders)
            .revenueGrowth(0.0)
            .totalSellers(totalSellers)
            .activeSellers(activeSellers)
            .avgDeliveryTime(avgDeliveryTime / 24.0) // Convert to days
            .onTimeDeliveryRate(onTimeRate)
            .returnRate(0.0)
            .totalUsers(totalUsers)
            .newUsers(0)
            .activeUsers(0)
            .conversionRate(0.0)
            .revenueLabels(revenueLabels)
            .revenueData(revenueData)
            .orderCountData(orderCountData)
            .categoryRevenue(categoryRevenue)
            .paymentMethods(paymentMethods)
            .topSellers(topSellers)
            .build();
    }
    
    private List<TopSellerDTO> getTopSellers(List<CustomerOrder> orders) {
        Map<Integer, BigDecimal> sellerRevenue = new HashMap<>();
        Map<Integer, Integer> sellerOrderCount = new HashMap<>();
        
        for (CustomerOrder order : orders) {
            if (order.getSellerId() != null && order.getTotalAmount() != null) {
                sellerRevenue.merge(order.getSellerId(), order.getTotalAmount(), BigDecimal::add);
                sellerOrderCount.merge(order.getSellerId(), 1, Integer::sum);
            }
        }
        
        return sellerRevenue.entrySet().stream()
            .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                Integer sellerId = entry.getKey();
                String shopName = "Shop #" + sellerId;
                
                try {
                    Optional<Seller> seller = sellerRepository.findById(sellerId);
                    if (seller.isPresent() && seller.get().getAccount() != null) {
                        Optional<Account> account = accountRepository.findById(seller.get().getAccount().getId());
                        if (account.isPresent()) {
                            shopName = account.get().getUsername();
                        }
                    }
                } catch (Exception ignored) {}
                
                return TopSellerDTO.builder()
                    .sellerId(sellerId)
                    .shopName(shopName)
                    .totalRevenue(entry.getValue())
                    .totalOrders(sellerOrderCount.getOrDefault(sellerId, 0))
                    .avgRating(4.5)
                    .status("Tốt")
                    .build();
            })
            .collect(Collectors.toList());
    }
}
