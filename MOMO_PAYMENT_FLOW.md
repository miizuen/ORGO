# 📋 FLOW THANH TOÁN MOMO VÀ QUẢN LÝ ĐƠN HÀNG

## 🔄 **FLOW HOÀN CHỈNH**

### **1. Khách hàng đặt hàng (Checkout)**
- **Hành động**: Customer click "Đặt hàng" và chọn thanh toán MoMo
- **Kết quả**:
  - Đơn hàng được tạo với:
    - `orderStatus = PENDING_PAYMENT` (Chờ thanh toán)
    - `paymentStatus = PENDING` (Chưa thanh toán)
  - Hệ thống gọi MoMo API để tạo payment link
  - Customer được redirect sang MoMo để thanh toán
- **File**: `CheckoutService.saveOrder()`

---

### **2. Khách hàng thanh toán trên MoMo**
- **Hành động**: Customer quét QR hoặc thanh toán trên app MoMo
- **Kết quả**:
  - MoMo xử lý thanh toán
  - MoMo gửi IPN (Instant Payment Notification) về server

---

### **3. Nhận IPN từ MoMo (Webhook)**
- **Hành động**: MoMo gọi webhook `/momo/ipn` với kết quả thanh toán
- **Xử lý**:
  - Verify chữ ký (signature) để đảm bảo request từ MoMo
  - Kiểm tra `resultCode`:
    - `resultCode = 0` → Thanh toán thành công
    - `resultCode != 0` → Thanh toán thất bại

#### **3a. Thanh toán THÀNH CÔNG (resultCode = 0)**
- **Cập nhật đơn hàng**:
  - `paymentStatus = PAID` (Đã thanh toán)
  - `orderStatus = PENDING` (Chờ seller duyệt)
  - `paidAt = now()`
- **Lưu lịch sử thanh toán** (PaymentHistory)
- **Chia tiền** (Revenue Distribution) cho seller, platform, expert
- **File**: `MomoPaymentServiceImpl.handleIpn()`

#### **3b. Thanh toán THẤT BẠI (resultCode != 0)**
- **Cập nhật đơn hàng**:
  - `paymentStatus = FAILED` (Thanh toán thất bại)
  - `orderStatus = CANCELLED` (Đơn hàng bị hủy)
- **Hoàn lại tồn kho** sản phẩm

---

### **4. Seller duyệt đơn hàng**
- **Điều kiện**: Đơn hàng phải ở trạng thái `PENDING` (đã thanh toán, chờ duyệt)
- **Hành động**: Seller click "Xác nhận đơn hàng"
- **Kết quả**:
  - `orderStatus = PROCESSING` (Đang xử lý/đóng gói)
  - `confirmedAt = now()`
- **File**: `SellerOrderService.confirmOrder()`

---

### **5. Seller giao cho đơn vị vận chuyển**
- **Điều kiện**: Đơn hàng phải ở trạng thái `PROCESSING`
- **Hành động**: Seller click "Đã giao cho đơn vị vận chuyển"
- **Kết quả**:
  - `orderStatus = SHIPPED` (Đang giao hàng)
  - `shippedAt = now()`
- **File**: `SellerOrderService.shipOrder()`

---

### **6. Khách hàng xác nhận đã nhận hàng**
- **Điều kiện**: Đơn hàng phải ở trạng thái `SHIPPED`
- **Hành động**: Customer click "Đã nhận hàng"
- **Kết quả**:
  - `orderStatus = DELIVERED` (Hoàn thành)
  - `deliveredAt = now()`
- **File**: `OrderService.confirmDelivery()`

---

## 📊 **BẢNG TRẠNG THÁI**

| Bước | Order Status | Payment Status | Người thực hiện | Mô tả |
|------|--------------|----------------|-----------------|-------|
| 1 | `PENDING_PAYMENT` | `PENDING` | Customer | Vừa tạo đơn, chờ thanh toán |
| 2 | `PENDING` | `PAID` | MoMo IPN | Đã thanh toán, chờ seller duyệt |
| 3 | `PROCESSING` | `PAID` | Seller | Seller đã duyệt, đang đóng gói |
| 4 | `SHIPPED` | `PAID` | Seller | Đã giao cho shipper |
| 5 | `DELIVERED` | `PAID` | Customer | Giao hàng thành công |

### **Trường hợp đặc biệt:**
- **Thanh toán thất bại**: `PENDING_PAYMENT` → `CANCELLED` + `FAILED`
- **Khách hủy đơn**: `PENDING` hoặc `PROCESSING` → `CANCELLED`
- **Hoàn trả**: `DELIVERED` → `RETURNED` + `REFUNDED`

---

## 🔐 **BẢO MẬT VÀ VALIDATION**

### **1. Verify Signature (Chữ ký)**
Mọi request từ MoMo đều phải verify signature:
```java
String raw = buildIpnSignature(request);
String expected = hmacSha256(raw, secretKey);
if (!expected.equalsIgnoreCase(request.getSignature())) {
    // Từ chối request
}
```

### **2. Kiểm tra Partner Code**
```java
if (!request.getPartnerCode().equals(momoProperties.getPartnerCode())) {
    // Từ chối request
}
```

### **3. Tránh duplicate transaction**
```java
PaymentHistory existing = paymentHistoryRepository.findByTransactionCode(transactionCode);
if (existing != null) {
    // Đã xử lý rồi, bỏ qua
}
```

---

## ⚙️ **CẤU HÌNH MÔITƯỜNG**

### **SANDBOX (Testing)**
```env
MOMO_PARTNER_CODE=MOMOBKUN20180529
MOMO_ACCESS_KEY=klm05TvNBzhg7h7j
MOMO_SECRET_KEY=at67qH6mk8w5Y1nAyMoYKMWACiEi2bsa
MOMO_ENDPOINT_CREATE=https://test-payment.momo.vn/v2/gateway/api/create
```

### **PRODUCTION**
```env
MOMO_PARTNER_CODE=<YOUR_PRODUCTION_PARTNER_CODE>
MOMO_ACCESS_KEY=<YOUR_PRODUCTION_ACCESS_KEY>
MOMO_SECRET_KEY=<YOUR_PRODUCTION_SECRET_KEY>
MOMO_ENDPOINT_CREATE=https://payment.momo.vn/v2/gateway/api/create
MOMO_REDIRECT_URL=https://yourdomain.com/momo/return
MOMO_IPN_URL=https://yourdomain.com/momo/ipn
```

**⚠️ LƯU Ý:**
- Production cần domain chính thức (không dùng ngrok)
- Phải đăng ký callback URLs với MoMo Business
- Tài khoản phải được MoMo duyệt và kích hoạt

---

## 🐛 **XỬ LÝ LỖI THƯỜNG GẶP**

### **1. ResultCode 13: "Cấu hình doanh nghiệp không chính xác"**
- **Nguyên nhân**: 
  - Dùng credentials sandbox với endpoint production
  - Credentials sai hoặc tài khoản chưa kích hoạt
- **Giải pháp**: 
  - Kiểm tra lại credentials
  - Đảm bảo endpoint và credentials cùng môi trường

### **2. IPN không nhận được**
- **Nguyên nhân**:
  - IPN URL không public (localhost, ngrok free)
  - Firewall chặn
- **Giải pháp**:
  - Dùng ngrok hoặc domain public
  - Kiểm tra logs server

### **3. Signature không khớp**
- **Nguyên nhân**:
  - Secret key sai
  - Thứ tự fields trong raw string sai
- **Giải pháp**:
  - Kiểm tra lại secret key
  - Xem logs để debug raw signature string

---

## 📝 **LOGS QUAN TRỌNG**

### **Khi tạo payment:**
```
INFO: MoMo create payment: orderId=123, orderCode=ORD-ABC, amount=50000
```

### **Khi nhận IPN:**
```
INFO: MoMo IPN accepted: orderCode=ORD-ABC, resultCode=0, mappedStatus=PAID
```

### **Khi chia tiền:**
```
INFO: MoMo revenue distribution started: orderId=123, orderCode=ORD-ABC
```

---

## ✅ **CHECKLIST TRIỂN KHAI PRODUCTION**

- [ ] Có tài khoản MoMo Business đã được duyệt
- [ ] Có credentials production (Partner Code, Access Key, Secret Key)
- [ ] Có domain chính thức (HTTPS)
- [ ] Đã đăng ký callback URLs với MoMo
- [ ] Đã test trên sandbox thành công
- [ ] Đã setup monitoring và logging
- [ ] Đã test IPN webhook
- [ ] Đã test flow hoàn chỉnh từ đầu đến cuối

---

**Tài liệu MoMo:** https://developers.momo.vn/
