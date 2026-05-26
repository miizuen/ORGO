-- Fix CHECK constraint để cho phép PENDING_PAYMENT

-- 1. Xóa constraint cũ
ALTER TABLE DonHang
DROP CONSTRAINT CK__DonHang__trang_t__4CA06362;

-- 2. Thêm constraint mới với PENDING_PAYMENT
ALTER TABLE DonHang
ADD CONSTRAINT CK_DonHang_trang_thai_don_hang
CHECK (trang_thai_don_hang IN (
    'PENDING_PAYMENT',  -- Chờ thanh toán (mới thêm)
    'PENDING',          -- Chờ xử lý
    'PROCESSING',       -- Đang xử lý
    'SHIPPED',          -- Đang giao
    'DELIVERED',        -- Hoàn thành
    'CANCELLED',        -- Đã hủy
    'RETURNED'          -- Hoàn trả
));

-- Kiểm tra constraint mới
SELECT 
    CONSTRAINT_NAME,
    CHECK_CLAUSE
FROM INFORMATION_SCHEMA.CHECK_CONSTRAINTS
WHERE TABLE_NAME = 'DonHang'
  AND CONSTRAINT_NAME LIKE '%trang_thai%';
