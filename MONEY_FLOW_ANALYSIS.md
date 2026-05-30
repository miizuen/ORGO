# 💰 PHÂN TÍCH LUỒNG TIỀN TRONG HỆ THỐNG ORGO

## 📊 TỔNG QUAN LUỒNG TIỀN

Khi user thanh toán đơn hàng thành công, tiền sẽ được **phân phối tự động** vào các ví (WalletBalance) của:
1. **Seller** (Người bán) - 95% giá trị đơn hàng
2. **Admin** (Nền tảng) - 3-5% giá trị đơn hàng
3. **Expert** (Chuyên gia) - 2% giá trị đơn hàng (nếu đơn hàng đến từ bài viết)

---

## 🔄 LUỒNG TIỀN CHI TIẾT (ĐÃ CẬP NHẬT - AN TOÀN HỚN)

### **Bước 1: User thanh toán qua MoMo**
- User quét QR hoặc thanh toán trên app MoMo
- Tiền được chuyển vào tài khoản MoMo của **ORGO Platform**
- **Tiền GIỮ Ở MOMO** - chưa chia cho ai
- MoMo gửi IPN (webhook) về server thông báo thanh toán thành công

**File xử lý:** `MomoPaymentServiceImpl.handleIpn()`

---

### **Bước 2: Cập nhật trạng thái đơn hàng**
Khi nhận IPN từ MoMo với `resultCode = 0` (thành công):
```java
order.setPaymentStatus(PaymentStatus.PAID);
order.setOrderStatus(OrderStatus.PENDING); // Chờ seller duyệt
order.setPaidAt(LocalDateTime.now());
```

**⚠️ QUAN TRỌNG:** Tiền **VẪN GIỮ Ở MOMO** - CHƯA chia cho seller/admin/expert!

---

### **Bước 3: Seller duyệt đơn hàng**
- Seller xác nhận đơn hàng trong dashboard
- Trạng thái chuyển từ `PENDING` → `PROCESSING`
- **KHÔNG chia tiền** ở bước này

**File xử lý:** `SellerOrderService.confirmOrder()`

```java
order.setOrderStatus(OrderStatus.PROCESSING);
order.setConfirmedAt(LocalDateTime.now());
orderRepository.save(order);

// ✅ KHÔNG chia tiền ở đây - tiền vẫn giữ ở MoMo
```

---

### **Bước 4: Seller giao hàng cho shipper**
- Seller đóng gói và giao cho đơn vị vận chuyển
- Trạng thái chuyển từ `PROCESSING` → `SHIPPED`
- **KHÔNG chia tiền** ở bước này

**File xử lý:** `SellerOrderService.shipOrder()`

```java
order.setOrderStatus(OrderStatus.SHIPPED);
order.setShippedAt(LocalDateTime.now());
orderRepository.save(order);

// ✅ KHÔNG chia tiền ở đây - tiền vẫn giữ ở MoMo
```

---

### **Bước 5: User xác nhận đã nhận hàng**
- User nhận hàng và click "Đã nhận hàng" trong đơn hàng
- Trạng thái chuyển từ `SHIPPED` → `DELIVERED`
- **✅ KÍCH HOẠT CHIA TIỀN TỰ ĐỘNG**

**File xử lý:** `OrderService.confirmDelivery()`

```java
order.setOrderStatus(OrderStatus.DELIVERED);
order.setDeliveredAt(LocalDateTime.now());
orderRepository.save(order);

// ✅ QUAN TRỌNG: Chia tiền được kích hoạt TẠI ĐÂY
try {
    revenueDistributionService.distributeForOrder(orderId);
    System.out.println("✅ Revenue distributed successfully");
} catch (Exception e) {
    System.err.println("❌ Failed to distribute revenue: " + e.getMessage());
}
```

---

### **Bước 6: Chia tiền vào ví (Revenue Distribution)**

**File xử lý:** `RevenueDistributionService.distributeForOrder()`

#### **6.1. Điều kiện để chia tiền:**
```java
// Đơn hàng phải đã thanh toán
if (order.getPaymentStatus() != PaymentStatus.PAID) 
    throw new RuntimeException("Don hang chua thanh toan");

// ✅ Đơn hàng phải ở trạng thái DELIVERED (user đã nhận hàng)
if (order.getOrderStatus() != OrderStatus.DELIVERED) 
    throw new RuntimeException("Don hang chua hoan thanh de chi tien");

// Chưa từng chia tiền cho đơn hàng này
if (!orderSettlementRepository.findByOrderId(orderId).isEmpty()) 
    return; // Đã chia rồi, bỏ qua
```

#### **4.2. Tính toán tỷ lệ chia:**

**Trường hợp 1: Đơn hàng KHÔNG có bài viết (mua trực tiếp)**
```
Seller:  95% (SELLER_RATIO = 0.95)
Admin:   5%  (ADMIN_RATIO_FULL = 0.05)
Expert:  0%
```

**Trường hợp 2: Đơn hàng CÓ bài viết (từ expert)**
```
Seller:  95% (SELLER_RATIO = 0.95)
Admin:   3%  (ADMIN_RATIO_SHARED = 0.03)
Expert:  2%  (EXPERT_RATIO = 0.02)
```

#### **4.3. Ví dụ cụ thể:**

**Đơn hàng 1,000,000 VND - KHÔNG có bài viết:**
```
Seller nhận:  950,000 VND (95%)
Admin nhận:    50,000 VND (5%)
Expert nhận:        0 VND
```

**Đơn hàng 1,000,000 VND - CÓ bài viết:**
```
Seller nhận:  950,000 VND (95%)
Admin nhận:    30,000 VND (3%)
Expert nhận:   20,000 VND (2%)
```

#### **4.4. Xử lý đơn hàng nhiều seller:**
Nếu đơn hàng có sản phẩm từ nhiều seller khác nhau:
```java
// Tính tổng tiền cho từng seller
Map<Integer, BigDecimal> sellerTotals = buildSellerTotals(orderId);

// Chia tiền riêng cho từng seller
for (Map.Entry<Integer, BigDecimal> entry : sellerTotals.entrySet()) {
    Integer sellerId = entry.getKey();
    BigDecimal sellerOrderAmount = entry.getValue();
    
    BigDecimal sellerAmount = sellerOrderAmount.multiply(SELLER_RATIO); // 95%
    BigDecimal adminAmount = sellerOrderAmount.multiply(adminRatio);    // 3-5%
    
    // Cộng tiền vào ví seller
    creditWallet(sellerAccountId, sellerAmount, "SELLER_PAYOUT", orderId, ...);
}
```

---

### **Bước 5: Cập nhật ví tiền (WalletBalance)**

**Cấu trúc bảng `SoDuVi` (WalletBalance):**
```sql
CREATE TABLE SoDuVi (
    id_vi INT PRIMARY KEY,
    id_tai_khoan INT,              -- ID tài khoản (seller/admin/expert)
    so_du_kha_dung DECIMAL(18,2),  -- Số dư có thể rút
    so_du_tam_giu DECIMAL(18,2),   -- Số dư đang chờ xử lý
    tong_da_rut DECIMAL(18,2),     -- Tổng đã rút
    so_du_duy_tri DECIMAL(18,2),   -- Số dư tối thiểu (20,000 VND)
    ngay_cap_nhat DATETIME
);
```

**Logic cộng tiền:**
```java
private void creditWallet(Integer accountId, BigDecimal amount, String type, 
                         Integer referenceId, String description) {
    // Tìm hoặc tạo ví mới
    WalletBalance wallet = walletBalanceRepository.findByAccountId(accountId)
        .orElseGet(() -> createNewWallet(accountId));
    
    // Cộng tiền vào số dư khả dụng
    wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
    wallet.setUpdatedAt(LocalDateTime.now());
    walletBalanceRepository.save(wallet);
    
    // Lưu lịch sử giao dịch
    saveTransactionHistory(wallet.getId(), type, amount, ...);
}
```

**Các loại giao dịch (type):**
- `SELLER_PAYOUT` - Tiền bán hàng của seller
- `ADMIN_COMMISSION` - Hoa hồng nền tảng
- `EXPERT_COMMISSION` - Hoa hồng chuyên gia
- `UNLOCK` - Hoàn tiền khi từ chối rút
- `WITHDRAWAL` - Rút tiền

---

### **Bước 6: Lưu lịch sử giao dịch (TransactionHistory)**

**Cấu trúc bảng `TransactionHistory`:**
```sql
CREATE TABLE TransactionHistory (
    id INT PRIMARY KEY,
    id_vi INT,                      -- ID ví
    loai_giao_dich NVARCHAR(50),    -- SELLER_PAYOUT, ADMIN_COMMISSION, etc.
    so_tien DECIMAL(18,2),          -- Số tiền giao dịch
    so_du_sau_giao_dich DECIMAL(18,2), -- Số dư sau khi giao dịch
    id_tham_chieu INT,              -- ID đơn hàng hoặc lệnh rút
    mo_ta NVARCHAR(255),            -- Mô tả giao dịch
    ngay_tao DATETIME
);
```

**Ví dụ record:**
```
id_vi: 5
loai_giao_dich: "SELLER_PAYOUT"
so_tien: 950000
so_du_sau_giao_dich: 2450000
id_tham_chieu: 123 (order ID)
mo_ta: "Nhan tien hang don ORD-20260524-001"
ngay_tao: 2026-05-24 18:51:00
```

---

### **Bước 7: Lưu bản ghi quyết toán (OrderSettlement)**

**Cấu trúc bảng `OrderSettlement`:**
```sql
CREATE TABLE OrderSettlement (
    id INT PRIMARY KEY,
    id_don_hang INT,                -- ID đơn hàng
    id_nha_ban_hang INT,            -- ID seller
    so_tien_don_hang DECIMAL(18,2), -- Tổng tiền đơn hàng của seller này
    so_tien_hoa_hong DECIMAL(18,2), -- Hoa hồng admin (3-5%)
    so_tien_nha_ban DECIMAL(18,2),  -- Tiền seller nhận (95%)
    trang_thai NVARCHAR(50),        -- "SETTLED"
    ngay_tao DATETIME
);
```

**Mục đích:** Ghi lại chi tiết chia tiền cho mỗi seller trong đơn hàng, phục vụ báo cáo và đối soát.

---

## 🏦 SELLER RÚT TIỀN

### **Bước 1: Seller tạo yêu cầu rút tiền**
```java
// File: PayoutServiceImpl.createRequest()
WalletBalance wallet = walletBalanceRepository.findByAccountId(accountId);

// Kiểm tra số dư
if (wallet.getAvailableBalance().compareTo(amount) < 0) {
    throw new IllegalArgumentException("So du khong du");
}

// Chuyển tiền từ "khả dụng" sang "tạm giữ"
wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
wallet.setHeldBalance(wallet.getHeldBalance().add(amount));
walletBalanceRepository.save(wallet);

// Tạo lệnh rút
WithdrawalRequest request = new WithdrawalRequest();
request.setRequesterId(accountId);
request.setAmount(amount);
request.setStatus(WithdrawalStatus.PENDING);
withdrawalRequestRepository.save(request);
```

### **Bước 2: Admin duyệt rút tiền**
```java
// File: PayoutServiceImpl.approveRequest()
WithdrawalRequest request = withdrawalRequestRepository.findById(requestId);
WalletBalance wallet = walletBalanceRepository.findByAccountId(request.getRequesterId());

// Chuyển tiền từ "tạm giữ" sang "đã rút"
wallet.setHeldBalance(wallet.getHeldBalance().subtract(amount));
wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
walletBalanceRepository.save(wallet);

// Cập nhật trạng thái lệnh rút
request.setStatus(WithdrawalStatus.APPROVED);
request.setApprovedAt(LocalDateTime.now());
withdrawalRequestRepository.save(request);

// Admin chuyển tiền thật qua ngân hàng cho seller
```

### **Bước 3: Admin từ chối rút tiền**
```java
// File: PayoutServiceImpl.rejectRequest()
// Hoàn tiền từ "tạm giữ" về "khả dụng"
wallet.setHeldBalance(wallet.getHeldBalance().subtract(amount));
wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
walletBalanceRepository.save(wallet);

request.setStatus(WithdrawalStatus.REJECTED);
withdrawalRequestRepository.save(request);
```

---

## ✅ GIẢI PHÁP ĐÃ TRIỂN KHAI - CHIA TIỀN AN TOÀN

### **Vấn đề cũ:**
- Chia tiền ngay khi seller duyệt đơn (`PROCESSING`)
- **Rủi ro:** Nếu user hủy đơn sau khi seller duyệt → Khó hoàn tiền vì đã chia cho seller/admin/expert

### **Giải pháp mới (ĐÃ TRIỂN KHAI):**
- **Giữ tiền ở MoMo** cho đến khi user xác nhận đã nhận hàng
- **Chỉ chia tiền** khi `orderStatus = DELIVERED`
- **Lợi ích:**
  - ✅ User hủy đơn trước khi nhận hàng → Tiền vẫn ở MoMo, dễ hoàn
  - ✅ Giảm tranh chấp và rủi ro tài chính
  - ✅ Bảo vệ cả buyer và seller

### **Code đã sửa:**

#### **1. MomoPaymentServiceImpl.handleIpn()**
```java
// ❌ XÓA logic chia tiền khi thanh toán thành công
// ✅ CHỈ cập nhật trạng thái, tiền giữ ở MoMo
if (mappedStatus == PaymentStatus.PAID) {
    order.setPaidAt(LocalDateTime.now());
    order.setOrderStatus(OrderStatus.PENDING);
    log.info("Money held at MoMo until delivery confirmed");
}
```

#### **2. OrderService.confirmDelivery()**
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

#### **3. RevenueDistributionService.distributeForOrder()**
```java
// ✅ SỬA điều kiện: Chỉ chia tiền khi DELIVERED
if (order.getOrderStatus() != OrderStatus.DELIVERED) 
    throw new RuntimeException("Don hang chua hoan thanh de chi tien");
```

#### **4. SellerOrderService.confirmOrder()**
```java
// ✅ KHÔNG chia tiền khi seller duyệt đơn
public boolean confirmOrder(Integer orderId) {
    order.setOrderStatus(OrderStatus.PROCESSING);
    order.setConfirmedAt(LocalDateTime.now());
    orderRepository.save(order);
    // Không gọi revenueDistributionService
    return true;
}
```

---

## 📈 FLOW CHART LUỒNG TIỀN (ĐÃ CẬP NHẬT)

```
User thanh toán MoMo
         ↓
💰 Tiền vào tài khoản MoMo của ORGO (GIỮ Ở ĐÂY)
         ↓
MoMo gửi IPN về server
         ↓
Cập nhật: paymentStatus = PAID, orderStatus = PENDING
         ↓
💰 Tiền VẪN Ở MOMO
         ↓
Seller duyệt đơn hàng
         ↓
Cập nhật: orderStatus = PROCESSING
         ↓
💰 Tiền VẪN Ở MOMO
         ↓
Seller giao hàng cho shipper
         ↓
Cập nhật: orderStatus = SHIPPED
         ↓
💰 Tiền VẪN Ở MOMO
         ↓
User nhận hàng và xác nhận
         ↓
Cập nhật: orderStatus = DELIVERED
         ↓
⚡ KÍCH HOẠT CHIA TIỀN TỰ ĐỘNG ⚡
         ↓
    ┌────┴────┬─────────┐
    ↓         ↓         ↓
Seller    Admin     Expert (nếu có)
  95%      3-5%        2%
    ↓         ↓         ↓
Cộng vào WalletBalance (so_du_kha_dung)
    ↓
Lưu TransactionHistory
    ↓
Lưu OrderSettlement
    ↓
Seller có thể rút tiền
```

### **Lợi ích của luồng mới:**
1. ✅ **An toàn hơn:** Tiền giữ ở MoMo cho đến khi giao dịch hoàn tất
2. ✅ **Dễ hoàn tiền:** Nếu user hủy đơn trước khi nhận hàng, tiền vẫn ở MoMo
3. ✅ **Giảm tranh chấp:** Seller chỉ nhận tiền khi user đã nhận hàng
4. ✅ **Bảo vệ buyer:** User có thể hủy đơn mà không lo tiền đã bị chia

---

## 🔍 KIỂM TRA LUỒNG TIỀN

### **Query kiểm tra ví tiền:**
```sql
-- Xem số dư ví của seller
SELECT 
    a.username,
    w.so_du_kha_dung AS available,
    w.so_du_tam_giu AS held,
    w.tong_da_rut AS withdrawn
FROM SoDuVi w
JOIN TaiKhoan a ON w.id_tai_khoan = a.id_tai_khoan
WHERE a.id_vai_tro = 3; -- ROLE_SELLER
```

### **Query kiểm tra lịch sử giao dịch:**
```sql
-- Xem lịch sử giao dịch của một đơn hàng
SELECT 
    th.loai_giao_dich,
    th.so_tien,
    th.mo_ta,
    th.ngay_tao,
    a.username
FROM TransactionHistory th
JOIN SoDuVi w ON th.id_vi = w.id_vi
JOIN TaiKhoan a ON w.id_tai_khoan = a.id_tai_khoan
WHERE th.id_tham_chieu = 123 -- Order ID
ORDER BY th.ngay_tao DESC;
```

### **Query kiểm tra quyết toán:**
```sql
-- Xem chi tiết chia tiền của đơn hàng
SELECT 
    os.id_don_hang,
    s.ten_shop,
    os.so_tien_don_hang AS order_amount,
    os.so_tien_nha_ban AS seller_amount,
    os.so_tien_hoa_hong AS admin_commission,
    os.trang_thai,
    os.ngay_tao
FROM OrderSettlement os
JOIN NhaBanHang s ON os.id_nha_ban_hang = s.id_nha_ban_hang
WHERE os.id_don_hang = 123;
```

---

## 📝 KẾT LUẬN

**Luồng tiền ĐÃ CẬP NHẬT (An toàn hơn):**
1. ✅ User thanh toán → Tiền vào tài khoản MoMo của ORGO
2. ✅ IPN cập nhật trạng thái đơn hàng → Tiền **GIỮ Ở MOMO**
3. ✅ Seller duyệt đơn → Tiền **VẪN Ở MOMO**
4. ✅ Seller giao hàng → Tiền **VẪN Ở MOMO**
5. ✅ **User xác nhận đã nhận hàng** → **KÍCH HOẠT CHIA TIỀN TỰ ĐỘNG**
6. ✅ Tiền được chia vào ví của Seller (95%), Admin (3-5%), Expert (2% nếu có)
7. ✅ Seller/Expert có thể xem số dư và tạo lệnh rút tiền
8. ✅ Admin duyệt lệnh rút → Chuyển tiền thật qua ngân hàng

**Các file đã sửa:**
- ✅ `MomoPaymentServiceImpl.handleIpn()` - Xóa logic chia tiền khi thanh toán
- ✅ `OrderService.confirmDelivery()` - Thêm logic chia tiền khi user xác nhận nhận hàng
- ✅ `RevenueDistributionService.distributeForOrder()` - Sửa điều kiện từ `PROCESSING` → `DELIVERED`
- ✅ `SellerOrderService.confirmOrder()` - Giữ nguyên, không chia tiền

**Lợi ích:**
- 🛡️ **Bảo vệ buyer:** User có thể hủy đơn trước khi nhận hàng mà không lo tiền đã bị chia
- 🛡️ **Bảo vệ seller:** Seller chỉ nhận tiền khi user đã xác nhận nhận hàng
- 💰 **Dễ hoàn tiền:** Nếu có tranh chấp, tiền vẫn ở MoMo, dễ xử lý
- ⚡ **Tự động hóa:** Chia tiền tự động khi user click "Đã nhận hàng"
