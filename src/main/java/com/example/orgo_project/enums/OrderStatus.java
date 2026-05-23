package com.example.orgo_project.enums;

public enum OrderStatus {
    PENDING_PAYMENT,  // Chờ thanh toán (online payment)
    PENDING,          // Đã thanh toán / COD → chờ seller duyệt
    PROCESSING,       // Seller đã duyệt, đang đóng gói
    SHIPPED,          // Đã giao shipper
    DELIVERED,        // Giao thành công
    CANCELLED,        // Đã hủy
    RETURNED          // Hoàn trả
}
