# ✅ CHIA TIỀN AN TOÀN - ĐÃ CẬP NHẬT

## 🎯 THAY ĐỔI CHÍNH

### **Trước đây (Rủi ro):**
- Chia tiền ngay khi seller duyệt đơn (`PROCESSING`)
- ❌ **Vấn đề:** User hủy đơn sau khi seller duyệt → Khó hoàn tiền vì đã chia

### **Bây giờ (An toàn):**
- Tiền giữ ở MoMo cho đến khi user xác nhận đã nhận hàng
- ✅ Chỉ chia tiền khi `orderStatus = DELIVERED`
- ✅ User hủy đơn trước khi nhận hàng → Tiền vẫn ở MoMo, dễ hoàn

---

## 📋 LUỒNG TIỀN MỚI

```
1. User thanh toán MoMo
   → 💰 Tiền vào tài khoản MoMo ORGO (GIỮ Ở ĐÂY)

2. MoMo IPN → Cập nhật: PAID + PENDING
   → 💰 Tiền VẪN Ở MOMO

3. Seller duyệt đơn → PROCESSING
   → 💰 Tiền VẪN Ở MOMO

4. Seller giao hàng → SHIPPED
   → 💰 Tiền VẪN Ở MOMO

5. User xác nhận "Đã nhận hàng" → DELIVERED
   → ⚡ KÍCH HOẠT CHIA TIỀN TỰ ĐỘNG
   → Seller 95% | Admin 3-5% | Expert 2%
   → Tiền vào ví WalletBalance
```

---

## 🔧 CÁC FILE ĐÃ SỬA

### **1. MomoPaymentServiceImpl.handleIpn()**
```java
// ❌ XÓA logic chia tiền khi thanh toán thành công
// ✅ CHỈ cập nhật trạng thái, tiền giữ ở MoMo
if (mappedStatus == PaymentStatus.PAID) {
    order.setPaidAt(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.PENDING);
    log.info("Money held at MoMo until delivery confirmed");
}
// KHÔNG gọi revenueDistributionService.distributeForOrder()
```

### **2. OrderService.confirmDelivery()**
```java
// ✅ THÊM logic chia tiền khi user xác nhận đã nhận hàng
public boolean confirmDelivery(Integer accountId, Integer orderId) {
    order.setOrderStatus(OrderStatus.DELIVERED);
    order.setDeliveredAt(LocalDateTime.now());
    orderRepository.save(order);
    
    // ✅ Kích hoạt chia tiền tự động
    try {
        revenueDistributionService.distributeForOrder(orderId);
        System.out.println("✅ Revenue distributed successfully");
    } catch (Exception e) {
        System.err.println("❌ Failed to distribute revenue");
    }
    
    return true;
}
```

### **3. RevenueDistributionService.distributeForOrder()**
```java
// ✅ SỬA điều kiện: Chỉ chia tiền khi DELIVERED
if (order.getOrderStatus() != OrderStatus.DELIVERED) 
    throw new RuntimeException("Don hang chua hoan thanh de chi tien");
```

### **4. SellerOrderService.confirmOrder()**
```java
// ✅ KHÔNG chia tiền khi seller duyệt đơn
public boolean confirmOrder(Integer orderId) {
    order.setOrderStatus(OrderStatus.PROCESSING);
    order.setConfirmedAt(LocalDateTime.now());
    orderRepository.save(order);
    // KHÔNG gọi revenueDistributionService
    return true;
}
```

---

## 📊 BẢNG TRẠNG THÁI VÀ TIỀN

| Trạng thái | Tiền ở đâu? | Ai thực hiện? | Chia tiền? |
|------------|-------------|---------------|------------|
| `PENDING_PAYMENT` | - | Customer | ❌ |
| `PENDING` | **MoMo (giữ)** | MoMo IPN | ❌ |
| `PROCESSING` | **MoMo (giữ)** | Seller | ❌ |
| `SHIPPED` | **MoMo (giữ)** | Seller | ❌ |
| `DELIVERED` | **Ví (chia)** | Customer | ✅ **CHIA TIỀN** |

---

## 🛡️ LỢI ÍCH

1. **Bảo vệ buyer:** User có thể hủy đơn trước khi nhận hàng mà không lo tiền đã bị chia
2. **Bảo vệ seller:** Seller chỉ nhận tiền khi user đã xác nhận nhận hàng
3. **Dễ hoàn tiền:** Nếu có tranh chấp, tiền vẫn ở MoMo, dễ xử lý
4. **Tự động hóa:** Chia tiền tự động khi user click "Đã nhận hàng"
5. **Giảm rủi ro:** Không phải xử lý hoàn tiền phức tạp từ nhiều ví

---

## 🧪 CÁCH KIỂM TRA

### **Test flow hoàn chỉnh:**
1. Tạo đơn hàng và thanh toán qua MoMo
2. Kiểm tra: `paymentStatus = PAID`, `orderStatus = PENDING`
3. Seller duyệt đơn → `orderStatus = PROCESSING`
4. Kiểm tra database: **CHƯA có record trong `OrderSettlement`**
5. Seller giao hàng → `orderStatus = SHIPPED`
6. Kiểm tra database: **VẪN CHƯA có record trong `OrderSettlement`**
7. User click "Đã nhận hàng" → `orderStatus = DELIVERED`
8. Kiểm tra database:
   - ✅ **CÓ record trong `OrderSettlement`**
   - ✅ **CÓ record trong `TransactionHistory`**
   - ✅ **Số dư trong `WalletBalance` đã tăng**

### **Query kiểm tra:**
```sql
-- Kiểm tra ví của seller
SELECT a.username, w.so_du_kha_dung 
FROM SoDuVi w
JOIN TaiKhoan a ON w.id_tai_khoan = a.id_tai_khoan
WHERE a.id_vai_tro = 3;

-- Kiểm tra lịch sử giao dịch
SELECT * FROM TransactionHistory 
WHERE id_tham_chieu = <order_id>
ORDER BY ngay_tao DESC;

-- Kiểm tra quyết toán
SELECT * FROM OrderSettlement 
WHERE id_don_hang = <order_id>;
```

---

## ⚠️ LƯU Ý

1. **Tiền thật vẫn ở MoMo:** Hệ thống chỉ ghi nhận số dư trong database. Admin cần chuyển tiền thật từ MoMo khi seller rút tiền.

2. **Xử lý hoàn tiền:** Nếu user hủy đơn sau khi đã xác nhận nhận hàng (`DELIVERED`), cần:
   - Trừ tiền từ ví seller/admin/expert
   - Hoàn tiền cho user qua MoMo
   - Cập nhật `orderStatus = RETURNED`

3. **Tự động xác nhận:** Có thể thêm cronjob tự động chuyển `SHIPPED` → `DELIVERED` sau X ngày nếu user không xác nhận.

---

## 📝 CHECKLIST TRIỂN KHAI

- [x] Sửa `MomoPaymentServiceImpl.handleIpn()` - Xóa logic chia tiền
- [x] Sửa `OrderService.confirmDelivery()` - Thêm logic chia tiền
- [x] Sửa `RevenueDistributionService.distributeForOrder()` - Đổi điều kiện sang `DELIVERED`
- [x] Kiểm tra `SellerOrderService.confirmOrder()` - Đảm bảo không chia tiền
- [ ] Test flow hoàn chỉnh trên môi trường dev
- [ ] Kiểm tra database sau mỗi bước
- [ ] Test trường hợp user hủy đơn ở các trạng thái khác nhau
- [ ] Deploy lên production
- [ ] Monitor logs để đảm bảo chia tiền hoạt động đúng

---

**Tài liệu chi tiết:** Xem `MONEY_FLOW_ANALYSIS.md`
