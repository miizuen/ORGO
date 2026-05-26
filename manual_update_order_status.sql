-- Cập nhật trạng thái đơn hàng thủ công (CHỈ DÙNG ĐỂ TEST)

-- 1. Xem đơn hàng hiện tại
SELECT 
    id_don_hang,
    maDonHang,
    trang_thai_don_hang,
    trang_thai_thanh_toan,
    tong_tien,
    ngay_dat,
    ngay_thanh_toan
FROM DonHang
WHERE maDonHang = 'ORD-0165DF21';  -- Thay bằng mã đơn hàng của bạn

-- 2. Cập nhật trạng thái thanh toán thành công
UPDATE DonHang
SET 
    trang_thai_thanh_toan = 'PAID',
    trang_thai_don_hang = 'PENDING',  -- Chờ seller duyệt
    ngay_thanh_toan = GETDATE()
WHERE maDonHang = 'ORD-0165DF21';  -- Thay bằng mã đơn hàng của bạn

-- 3. Thêm payment history
INSERT INTO LichSuThanhToan (
    id_don_hang,
    id_phuong_thuc,
    ma_giao_dich,
    so_tien,
    trang_thai,
    thoi_gian_giao_dich,
    ghi_chu
)
SELECT 
    id_don_hang,
    id_phuong_thuc,
    'MANUAL-TEST-' + maDonHang,
    tong_tien,
    'PAID',
    GETDATE(),
    'Manual update for testing'
FROM DonHang
WHERE maDonHang = 'ORD-0165DF21';  -- Thay bằng mã đơn hàng của bạn

-- 4. Kiểm tra kết quả
SELECT 
    d.id_don_hang,
    d.maDonHang,
    d.trang_thai_don_hang,
    d.trang_thai_thanh_toan,
    d.tong_tien,
    d.ngay_thanh_toan,
    l.ma_giao_dich,
    l.trang_thai AS payment_history_status
FROM DonHang d
LEFT JOIN LichSuThanhToan l ON d.id_don_hang = l.id_don_hang
WHERE d.maDonHang = 'ORD-0165DF21';  -- Thay bằng mã đơn hàng của bạn
