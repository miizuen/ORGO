package com.example.orgo_project.service;

import com.example.orgo_project.dto.CouponValidateRequestDTO;
import com.example.orgo_project.dto.CouponValidateResponseDTO;
import com.example.orgo_project.entity.DiscountCode;
import com.example.orgo_project.enums.DiscountStatus;
import com.example.orgo_project.repository.IDiscountCodeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CouponService implements ICouponService {

    private final IDiscountCodeRepository discountCodeRepository;

    public CouponService(IDiscountCodeRepository discountCodeRepository) {
        this.discountCodeRepository = discountCodeRepository;
    }

    @Override
    public CouponValidateResponseDTO validateCoupon(CouponValidateRequestDTO request) {
        if (request == null || request.getCode() == null || request.getCode().isBlank()) {
            return new CouponValidateResponseDTO(
                    false,
                    "Vui lòng nhập mã giảm giá",
                    null,
                    BigDecimal.ZERO,
                    request != null ? request.getOrderAmount() : BigDecimal.ZERO
            );
        }

        DiscountCode coupon = discountCodeRepository.findByCodeIgnoreCase(request.getCode().trim());
        if (coupon == null) {
            return new CouponValidateResponseDTO(
                    false,
                    "Mã giảm giá không tồn tại",
                    request.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (coupon.getStatus() != DiscountStatus.ACTIVE) {
            return new CouponValidateResponseDTO(
                    false,
                    "Mã giảm giá không hoạt động",
                    coupon.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            return new CouponValidateResponseDTO(
                    false,
                    "Mã giảm giá chưa đến thời gian áp dụng",
                    coupon.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            return new CouponValidateResponseDTO(
                    false,
                    "Mã giảm giá đã hết hạn",
                    coupon.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        if (coupon.getMinimumOrderAmount() != null
                && request.getOrderAmount() != null
                && request.getOrderAmount().compareTo(coupon.getMinimumOrderAmount()) < 0) {
            return new CouponValidateResponseDTO(
                    false,
                    "Đơn hàng chưa đạt giá trị tối thiểu",
                    coupon.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        if (coupon.getMaximumUsage() != null
                && coupon.getUsedCount() != null
                && coupon.getUsedCount() >= coupon.getMaximumUsage()) {
            return new CouponValidateResponseDTO(
                    false,
                    "Mã giảm giá đã hết lượt sử dụng",
                    coupon.getCode(),
                    BigDecimal.ZERO,
                    request.getOrderAmount()
            );
        }

        BigDecimal discountAmount = coupon.getDiscountValue() != null ? coupon.getDiscountValue() : BigDecimal.ZERO;
        if (request.getOrderAmount() != null && discountAmount.compareTo(request.getOrderAmount()) > 0) {
            discountAmount = request.getOrderAmount();
        }

        BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        return new CouponValidateResponseDTO(
                true,
                "Mã giảm giá hợp lệ",
                coupon.getCode(),
                discountAmount,
                finalAmount
        );
    }
}